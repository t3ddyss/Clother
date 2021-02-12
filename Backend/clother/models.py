from clother import db
from datetime import datetime


class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(50), unique=True, nullable=False)
    password = db.Column(db.String(60), nullable=False)
    created_at = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)
    email_verified = db.Column(db.Boolean, nullable=False, default=False)
    admin = db.Column(db.Boolean, nullable=False, default=False)
    name = db.Column(db.String(50), nullable=False)
    image = db.Column(db.String(50), default=None)

    def get_description(self):
        return f'User {self.name} with email {self.email}.'

    def to_json(self):
        return {'id': self.id, 'email': self.email, 'password': self.password, 'name': self.name}
