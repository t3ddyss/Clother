import math
import json
import random
import datetime
import time

import click
from flask import Blueprint

from sqlalchemy.exc import IntegrityError

from clother import db
from clother.users.models import User
from clother.offers.models import Offer, Category, Image, Location

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


@blueprint.cli.command('create_db')
def create_database():
    db.create_all()


@blueprint.cli.command('populate_categories')
def populate_categories():
    categories = json.load(open("./categories.json", 'r'))

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


@blueprint.cli.command('mock_users')
def mock_users():
    users = json.load(open("./users.json", 'r'))

    for item in users:
        user = User(email=item['email'],
                    name=item['first_name'] + ' ' + item['last_name'],
                    email_verified=True)
        user.set_password('qwerty')

        db.session.add(user)
        db.session.commit()

    print("Finished mocking users")


@blueprint.cli.command('mock_offers')
def mock_offers():
    offers = json.load(open("./offers.json", 'r'))

    for item in offers:
        offer = Offer(title=item['title'],
                      category_id=item['category_id'],
                      user_id=get_random_user(),
                      created_at=generate_random_time(),
                      size=get_random_size())
        for uri in item['images']:
            offer.images.append(Image(uri="https:" + uri))

        lat, lng = generate_random_location()
        offer.location = Location(latitude=lat, longitude=lng)

        try:
            db.session.add(offer)
            db.session.commit()
        except IntegrityError:
            db.session.rollback()

    print("Finished mocking offers")


def generate_random_time():
    min_time = datetime.datetime(year=2021, month=1, day=1)
    max_time = datetime.datetime(year=2021, month=3, day=31)

    min_time_ts = int(time.mktime(min_time.timetuple()))
    max_time_ts = int(time.mktime(max_time.timetuple()))

    random_ts = random.randint(min_time_ts, max_time_ts)
    return datetime.datetime.fromtimestamp(random_ts)


def generate_random_location(x0=55.7541, y0=37.62082, radius=17_500):
    radius_in_degrees = radius / (111.32 * 1000 * math.cos(x0 * (math.pi / 180)))

    u = random.uniform(0, 1)
    v = random.uniform(0, 1)
    w = radius_in_degrees * math.sqrt(u)
    t = 2 * math.pi * v
    x = w * math.cos(t)
    y = w * math.sin(t)

    return [x / math.cos(math.radians(y0)) + x0, y + y0]


def get_random_size():
    sizes = ["XS", "S", "M", "L", "XL"] + [str(x) for x in range(6, 12)]
    return random.choice(sizes)


def get_random_user():
    return random.choice(User.query.all()).id
