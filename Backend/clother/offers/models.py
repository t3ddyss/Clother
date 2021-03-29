from clother import db


class Offer(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    category_id = db.Column(db.Integer, db.ForeignKey('category.id'), nullable=False)
    title = db.Column(db.String, nullable=False)
    category = db.relationship('Category', backref=db.backref('offers', lazy=True))
    images = db.relationship('Image')

    def to_dict(self):
        return {'id': self.id,
                'title': self.title,
                'category_id': self.category_id,
                'image': [image.uri for image in self.images][0]}


class Category(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    parent_id = db.Column(db.Integer, db.ForeignKey('category.id'), nullable=True)
    title = db.Column(db.String, nullable=False)
    last_level = db.Column(db.Boolean, default=True)

    subcategories = db.relationship('Category',
                                    backref=db.backref('parent', remote_side=[id]),
                                    lazy=True) #post_update=True

    def to_dict(self):
        return {'id': self.id,
                'parent_id': self.parent_id,
                'title': self.title,
                'last_level': self.last_level}


class Image(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    offer_id = db.Column(db.Integer, db.ForeignKey('offer.id', ondelete='CASCADE'))
    uri = db.Column(db.String, unique=True)
