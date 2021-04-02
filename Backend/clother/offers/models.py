from datetime import datetime

from clother import db


class Offer(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    category_id = db.Column(db.Integer, db.ForeignKey('category.id'), nullable=False)
    created_at = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)
    title = db.Column(db.String, nullable=False)
    description = db.Column(db.String)

    user = db.relationship('User', uselist=False, backref=db.backref('offers', lazy=True))
    category = db.relationship('Category', uselist=False, backref=db.backref('offers', lazy=True))
    images = db.relationship('Image')
    size = db.relationship('Size', uselist=False)
    location = db.relationship('Location', uselist=False)

    def to_dict(self):
        return {'id': self.id,
                'user_id': self.user_id,
                'category_id': self.category_id,
                'title': self.title,
                'description': self.description,
                'images': [image.uri for image in self.images],
                'size': self.size,
                'location': self.location.to_string()}


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


class Image(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    offer_id = db.Column(db.Integer, db.ForeignKey('offer.id', ondelete='CASCADE'))
    uri = db.Column(db.String, unique=True, nullable=False)


class Size(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    offer_id = db.Column(db.Integer, db.ForeignKey('offer.id', ondelete='CASCADE'))
    type = db.Column(db.String, default='clothes')
    size = db.Column(db.String)


class Location(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    offer_id = db.Column(db.Integer, db.ForeignKey('offer.id', ondelete='CASCADE'))
    latitude = db.Column(db.Float, nullable=False)
    longitude = db.Column(db.Float, nullable=False)

    def to_string(self):
        return f'{self.latitude},{self.longitude}'
