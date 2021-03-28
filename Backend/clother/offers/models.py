from clother import db


class Offer(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String, nullable=False)
    image = db.Column(db.String, unique=True, nullable=False)
    address = db.Column(db.String, nullable=False)

    def to_dict(self):
        return {'id': self.id,
                'title': self.title,
                'image': self.image,
                'address': self.address}


class Category(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    parent_id = db.Column(db.Integer, db.ForeignKey('category.id'), nullable=True)
    title = db.Column(db.String, nullable=False)
