import os
from datetime import datetime

from flask import current_app
from sqlalchemy import event

from clother import db
from clother.images.models import BaseImage

user_chat = db.Table('user_chat', db.Model.metadata,
                     db.Column('user_id', db.Integer, db.ForeignKey('user.id')),
                     db.Column('chat_id', db.Integer, db.ForeignKey('chat.id')))


class Chat(db.Model):
    id = db.Column(db.Integer, primary_key=True)

    users = db.relationship('User',
                            secondary=user_chat,
                            backref=db.backref('chats', lazy=True))
    messages = db.relationship('Message', cascade="all,delete", lazy='dynamic')

    def to_dict(self, url_root, addressee_id):
        interlocutor = [x for x in self.users if x.id != addressee_id][0]
        last_message = self.messages[-1]
        return {'id': self.id,
                'interlocutor': interlocutor.to_dict(),
                'last_message': last_message.to_dict(url_root=url_root)}


class Message(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id', ondelete='CASCADE'))
    chat_id = db.Column(db.Integer, db.ForeignKey('chat.id', ondelete='CASCADE'))
    created_at = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)
    body = db.Column(db.Text, nullable=True)

    images = db.relationship('MessageImage', passive_deletes=True)
    user = db.relationship('User', uselist=False)

    def to_dict(self, url_root):
        return {'id': self.id,
                'chat_id': self.chat_id,
                'user_id': self.user_id,
                'user_name': self.user.name,
                'created_at': self.created_at.isoformat(' ', 'seconds'),
                'body': self.body,
                'images': [image.get_uri(url_root) for image in self.images]}


class MessageImage(BaseImage):
    __tablename__ = 'message_image'
    message_id = db.Column(db.Integer, db.ForeignKey('message.id', ondelete='CASCADE'), nullable=False)


@event.listens_for(Message, 'before_delete')
def execute_before_message_deletion(mapper, connection, message):
    for image in message.images:
        if image.is_local():
            os.remove(os.path.join(current_app.config['UPLOAD_FOLDER'], image.uri))
