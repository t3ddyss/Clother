import os
from datetime import datetime

from flask import current_app, request
from sqlalchemy import func, event
from sqlalchemy.ext.hybrid import hybrid_method

from clother import db
from clother.images.models import BaseImage


class Offer(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    category_id = db.Column(db.Integer, db.ForeignKey('category.id'), nullable=False)
    created_at = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)
    title = db.Column(db.String, nullable=False)
    description = db.Column(db.String)
    size = db.Column(db.String)

    user = db.relationship('User', uselist=False, backref=db.backref('offers', lazy=True))
    category = db.relationship('Category', uselist=False, backref=db.backref('offers', lazy=True))
    images = db.relationship('OfferImage', passive_deletes=True)
    location = db.relationship('Location', uselist=False, passive_deletes=True)

    def to_dict(self, url_root):
        return {'id': self.id,
                'user': self.user.to_dict(request.url_root),
                'category_id': self.category_id,
                'created_at': self.created_at.isoformat(sep=' ', timespec='seconds'),
                'title': self.title,
                'description': self.description,
                'size': self.size,
                'category': self.category.title,
                'images': [image.get_uri(url_root) for image in self.images],
                'location': self.location.to_string() if self.location else None}


class Category(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    parent_id = db.Column(db.Integer, db.ForeignKey('category.id'), nullable=True)
    title = db.Column(db.String, nullable=False)

    subcategories = db.relationship('Category',
                                    backref=db.backref('parent', remote_side=[id]),
                                    lazy=True)

    def to_dict(self):
        return {'id': self.id,
                'parent_id': self.parent_id,
                'title': self.title}


class OfferImage(BaseImage):
    __tablename__ = 'offer_image'
    offer_id = db.Column(db.Integer, db.ForeignKey('offer.id', ondelete='CASCADE'), nullable=False)


class Location(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    offer_id = db.Column(db.Integer, db.ForeignKey('offer.id', ondelete='CASCADE'), nullable=False)
    latitude = db.Column(db.Float, nullable=False)
    longitude = db.Column(db.Float, nullable=False)

    @hybrid_method
    def distance(self, lat2, lon2):
        return func.acos(func.sin(func.radians(self.latitude)) *
                         func.sin(func.radians(lat2)) +
                         func.cos(func.radians(self.latitude)) *
                         func.cos(func.radians(lat2)) *
                         func.cos(func.radians(lon2) - func.radians(self.longitude))) * 6371

    def to_string(self):
        return f'{self.latitude},{self.longitude}'


@event.listens_for(Offer, 'before_delete')
def execute_before_offer_deletion(mapper, connection, offer):
    for image in offer.images:
        if image.is_local():
            os.remove(os.path.join(current_app.config['UPLOAD_FOLDER'], image.uri))
