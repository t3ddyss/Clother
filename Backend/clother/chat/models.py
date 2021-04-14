from datetime import datetime

from clother import db

user_chat = db.Table('user_chat', db.Model.metadata,
                     db.Column('user_id', db.Integer, db.ForeignKey('user.id')),
                     db.Column('chat_id', db.Integer, db.ForeignKey('chat.id'))
                     )


class Chat(db.Model):
    id = db.Column(db.Integer, primary_key=True)

    users = db.relationship('User',
                            secondary=user_chat,
                            backref=db.backref('chats', lazy=True))
    messages = db.relationship('Message', cascade="all,delete", lazy='dynamic')

    def to_dict(self, user_id_to):
        interlocutor = [x for x in self.users if x.id != user_id_to][0]
        last_message = self.messages[-1]
        return {'id': self.id,
                'interlocutor': interlocutor.to_dict(),
                'last_message': last_message.to_dict()}


class Message(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id', ondelete='CASCADE'))
    chat_id = db.Column(db.Integer, db.ForeignKey('chat.id', ondelete='CASCADE'))
    created_at = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)
    body = db.Column(db.Text, nullable=False)
    image = db.Column(db.String)

    user = db.relationship('User', uselist=False)

    def to_dict(self):
        message = {'id': self.id,
                   'chat_id': self.chat_id,
                   'user_id': self.user_id,
                   'user_name': self.user.name,
                   'created_at': self.created_at.isoformat(' ', 'seconds'),
                   'body': self.body}
        if self.image:
            message['image'] = self.image

        return message
