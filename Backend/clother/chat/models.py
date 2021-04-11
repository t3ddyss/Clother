from datetime import datetime

from clother import db

user_chat_association = db.Table('user_chat', db.Model.metadata,
                                 db.Column('user_id', db.Integer, db.ForeignKey('user.id')),
                                 db.Column('chat_id', db.Integer, db.ForeignKey('chat.id'))
                                 )


class Chat(db.Model):
    id = db.Column(db.Integer, primary_key=True)

    users = db.relationship('User',
                            secondary=user_chat_association,
                            backref=db.backref('chats', lazy=True))
    messages = db.relationship('Message', cascade="all,delete")


class Message(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id', ondelete='CASCADE'))
    chat_id = db.Column(db.Integer, db.ForeignKey('chat.id', ondelete='CASCADE'))
    created_at = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)
    body = db.Column(db.Text, nullable=False)
    image = db.Column(db.String)

    def to_dict(self):
        message = {'id': self.id,
                   'user_id': self.user_id,
                   'created_at': self.created_at,
                   'body': self.body}
        if self.images:
            message['images'] = [image.uri for image in self.images]

        return message
