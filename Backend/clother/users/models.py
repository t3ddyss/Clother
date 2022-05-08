from clother import db
from datetime import datetime
from werkzeug.security import generate_password_hash, check_password_hash


class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(50), unique=True, nullable=False)
    password = db.Column(db.String(100), nullable=False)
    created_at = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)
    email_verified = db.Column(db.Boolean, nullable=False, default=False)
    name = db.Column(db.String(50), nullable=False)
    age = db.Column(db.Integer, default=None)
    image = db.Column(db.String(50), default=None)
    admin = db.Column(db.Boolean, nullable=False, default=False)

    # TODO add multiple sessions support
    is_connected = db.Column(db.Boolean, nullable=False, default=False)
    device_token = db.Column(db.String(255))

    def set_password(self, password):
        self.password = generate_password_hash(password, method='sha256')

    def check_password(self, password):
        return check_password_hash(self.password, password)

    def to_dict(self):
        return {'id': self.id,
                'name': self.name,
                'image': self.image}

    def to_details_dict(self):
        data = self.to_dict()
        details = {'email': self.email,
                   'created_at': self.created_at.isoformat(' ', 'seconds'),
                   'age': self.age}
        data['details'] = details
        return data
