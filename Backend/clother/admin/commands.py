import json
import click
from flask import Blueprint

from sqlalchemy.exc import IntegrityError

from clother import db
from clother.users.models import User
from clother.offers.models import Offer, Category, Image

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
    Category.__table__.create(db.engine)

    categories = json.load(open("./categories.json", 'r'))
    for entry in categories:
        category = Category(parent_id=entry['parent_id'], title=entry['title'])

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


@blueprint.cli.command('populate_offers')
def populate_offers():
    Offer.__table__.create(db.engine)
    Image.__table__.create(db.engine)

    offers = json.load(open("./offers.json", 'r'))
    for item in offers:
        offer = Offer(title=item['title'],
                      category_id=item['category_id'])
        for uri in item['images']:
            offer.images.append(Image(uri="https:" + uri))

        try:
            db.session.add(offer)
            db.session.commit()
        except IntegrityError:
            db.session.rollback()


@blueprint.cli.command('get_first_offer')
def get_first_offer():
    print(Offer.query.first().to_dict())


@blueprint.cli.command('get_subcats')
def get_subcats():
    print([x.to_dict() for x in Category.query.filter_by(id=4).first().subcategories])
    print([x.parent.to_dict() for x in Category.query.filter_by(id=4).first().subcategories])

