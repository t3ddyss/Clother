import os

from flask import current_app
from sqlalchemy import event

from clother import db
from datetime import datetime
from werkzeug.security import generate_password_hash, check_password_hash

from clother.images.models import BaseImage


class UserImage(BaseImage):
    __tablename__ = 'user_image'
    user_id = db.Column(db.Integer, db.ForeignKey('user.id', ondelete='CASCADE'), nullable=False)


class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(50), unique=True, nullable=False)
    password = db.Column(db.String(100), nullable=False)
    created_at = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)
    email_verified = db.Column(db.Boolean, nullable=False, default=False)
    name = db.Column(db.String(50), nullable=False)
    age = db.Column(db.Integer, default=None)
    status = db.Column(db.String(70), default=None)
    admin = db.Column(db.Boolean, nullable=False, default=False)

    # TODO add multiple sessions support
    is_connected = db.Column(db.Boolean, nullable=False, default=False)
    device_token = db.Column(db.String(255))

    image = db.relationship('UserImage', uselist=False)

    def set_password(self, password):
        self.password = generate_password_hash(password, method='sha256')

    def check_password(self, password):
        return check_password_hash(self.password, password)

    def to_dict(self, url_root):
        return {'id': self.id,
                'name': self.name,
                'image': self.image.get_uri(url_root) if self.image else None}

    def to_details_dict(self, url_root):
        data = self.to_dict(url_root)
        details = {'email': self.email,
                   'created_at': self.created_at.isoformat(' ', 'seconds'),
                   'status': self.status}
        data['details'] = details
        return data


@event.listens_for(UserImage, 'before_delete')
def execute_before_message_deletion(mapper, connection, image):
    if image.is_local():
        os.remove(os.path.join(current_app.config['UPLOAD_FOLDER'], image.uri))
