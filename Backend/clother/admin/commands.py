import json

from flask import Blueprint
from random import Random

from clother import db
from clother.admin.utils import get_random_user, get_random_size, generate_random_date, generate_random_location
from clother.chat.models import Chat, Message
from clother.offers.models import Offer, Category, OfferImage, Location
from clother.users.models import User

blueprint = Blueprint('admin', __name__)


@blueprint.cli.command('create-tables')
def create_tables():
    db.drop_all()
    db.create_all()
    db.session.commit()

    print("Database tables were successfully created")


@blueprint.cli.command('populate-categories')
def populate_categories():
    categories = json.load(open('clother/static/categories.json', 'r'))

    for item in categories:
        category = Category(id=item['id'], parent_id=item['parent_id'], title=item['title'])
        db.session.add(category)

    db.session.commit()
    print("Successfully populated categories")


@blueprint.cli.command('mock-users')
def mock_users():
    User.query.delete()
    users = json.load(open('clother/static/users.json', 'r'))

    for item in users:
        user = User(email=item['email'],
                    name=item['first_name'] + ' ' + item['last_name'],
                    email_verified=True)
        user.set_password('qwerty')
        db.session.add(user)

    db.session.commit()
    print("Successfully mocked users")


@blueprint.cli.command('mock-offers')
def mock_offers():
    Offer.query.delete()
    db.session.commit()

    offers = json.load(open('clother/static/offers.json', 'r'))
    Random(5).shuffle(offers)

    for item in offers:
        offer = Offer(title=item['title'],
                      category_id=item['category_id'],
                      user_id=get_random_user(),
                      created_at=generate_random_date(),
                      size=get_random_size())
        for uri in item['images']:
            offer.images.append(OfferImage(uri=uri))

        lat, lng = generate_random_location()
        offer.location = Location(latitude=lat, longitude=lng)

        db.session.add(offer)

    db.session.commit()
    print("Successfully mocked offers")


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

    print("Successfully mocked messages")
