import json
import time

import click
from flask import Blueprint
from sqlalchemy.exc import IntegrityError

from clother import db
from clother.admin.utils import get_random_user, get_random_size, generate_random_time, generate_random_location
from clother.chat.models import Chat, Message
from clother.offers.models import Offer, Category, Image, Location
from clother.users.models import User

blueprint = Blueprint('admin', __name__)


@blueprint.cli.command('promote')
@click.argument('email')
def promote(email):
    user = User.query.filter_by(email=email).first()

    if user:
        user.admin = True
        db.session.commit()
        print(f'Successfully promoted {email} to admin')
    else:
        print(f"User with email {email} doesn't exist")


@blueprint.cli.command('demote')
@click.argument('email')
def demote(email):
    user = User.query.filter_by(email=email).first()

    if user and user.admin:
        user.admin = False
        db.session.commit()
        print(f'{email} is no longer admin')
    else:
        print(f"Admin with email {email} doesn't exist")


@blueprint.cli.command('create-db')
def create_database():
    db.drop_all()
    db.create_all()
    db.session.commit()

    print('Database tables were successfully created')


@blueprint.cli.command('populate-categories')
def populate_categories():
    categories = json.load(open("clother/static/categories.json", 'r'))

    for item in categories:
        category = Category(parent_id=item['parent_id'], title=item['title'])

        db.session.add(category)
        db.session.commit()

    delete_query = Category.__table__.delete().where(Category.title.ilike("view all"))
    db.session.execute(delete_query)
    db.session.commit()

    categories = Category.query.all()
    for category in categories:
        if len(category.subcategories) > 0:
            category.last_level = False
            db.session.commit()

    print('Successfully populated categories')


@blueprint.cli.command('mock-users')
def mock_users():
    User.query.delete()
    users = json.load(open("clother/static/users.json", 'r'))

    for item in users:
        user = User(email=item['email'],
                    name=item['first_name'] + ' ' + item['last_name'],
                    email_verified=True)
        user.set_password('qwerty')

        db.session.add(user)
        db.session.commit()

    print('Successfully mocked users')


@blueprint.cli.command('mock-offers')
def mock_offers():
    Offer.query.delete()
    db.session.commit()

    offers = json.load(open("clother/static/offers.json", 'r'))

    for i, item in enumerate(offers):
        offer = Offer(title=item['title'],
                      category_id=item['category_id'],
                      user_id=get_random_user(),
                      created_at=generate_random_time(),
                      size=get_random_size())
        for uri in reversed(item['images']):
            offer.images.append(Image(uri="https:" + uri))

        lat, lng = generate_random_location()
        offer.location = Location(latitude=lat, longitude=lng)

        try:
            db.session.add(offer)
            db.session.commit()
        except IntegrityError:
            db.session.rollback()

    print('Successfully mocked offers')


@blueprint.cli.command('mock-messages')
def mock_messages():
    messages = ["Hello!",
                "Hi!",
                "I would like to swap my T-shirt for your hat",
                "Ok, fine",
                "Are you free on Friday?",
                "Yes"]
    Chat.query.delete()
    db.session.commit()

    users = User.query.order_by(User.id.asc()).limit(3).all()

    for i in range(len(users)):
        for j in range(i, len(users)):

            if users[i].id == users[j].id:
                continue

            chat = Chat()
            chat.users.extend([users[i], users[j]])
            db.session.add(chat)
            db.session.commit()

            for k in range(len(messages)):
                sender_id = users[i].id if k % 2 == 0 else users[j].id
                message = Message(user_id=sender_id, chat_id=chat.id, body=messages[k] + str(sender_id))
                chat.messages.append(message)
                db.session.commit()
                time.sleep(0.25)

    print('Successfully mocked messages')
