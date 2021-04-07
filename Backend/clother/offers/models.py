from datetime import datetime

from clother import db


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
    images = db.relationship('Image', cascade="all,delete")
    location = db.relationship('Location', uselist=False, cascade="all,delete")

    def to_dict(self):
        offer = {'id': self.id,
                 'user_id': self.user_id,
                 'category_id': self.category_id,
                 'created_at': self.created_at,
                 'title': self.title,
                 'description': self.description,
                 'size': self.size,
                 'user_name': self.user.name,
                 'category': self.category.title,
                 'images': [image.uri for image in self.images]}
        if self.location:
            offer['location'] = self.location.to_string()
        return offer


class Category(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    parent_id = db.Column(db.Integer, db.ForeignKey('category.id'), nullable=True)
    title = db.Column(db.String, nullable=False)
    last_level = db.Column(db.Boolean, default=True)

    subcategories = db.relationship('Category',
                                    backref=db.backref('parent', remote_side=[id]),
                                    lazy=True)

    def to_dict(self):
        return {'id': self.id,
                'parent_id': self.parent_id,
                'title': self.title,
                'last_level': self.last_level}


# TODO add event for deleting images from folder when image object is deleted
class Image(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    offer_id = db.Column(db.Integer, db.ForeignKey('offer.id', ondelete='CASCADE'))
    uri = db.Column(db.String, unique=True, nullable=False)


class Location(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    offer_id = db.Column(db.Integer, db.ForeignKey('offer.id', ondelete='CASCADE'))
    latitude = db.Column(db.Float, nullable=False)
    longitude = db.Column(db.Float, nullable=False)

    def to_string(self):
        return f'{self.latitude},{self.longitude}'
