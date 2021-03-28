import json
import click
from flask import Blueprint

from sqlalchemy.exc import IntegrityError

from clother import db
from clother.users.models import User
from clother.offers.models import Offer, Category

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


@blueprint.cli.command('populate_categories')
def populate_categories():
    Category.__table__.create(db.engine)

    categories = json.load(open("./categories.json", 'r'))
    for item in categories:
        category = Category(parent_id=None, title="First category")


    db.session.add(category)
    db.session.commit()


@blueprint.cli.command('populate_offers')
def populate_offers():
    Offer.__table__.create(db.engine)

    offers = json.load(open("./instance./offers.json", 'r'))
    for item in offers:
        offer = Offer(title=item['title'], image=item['image'], address=item['address'])

        try:
            db.session.add(offer)
            db.session.commit()
        except IntegrityError:
            db.session.rollback()
