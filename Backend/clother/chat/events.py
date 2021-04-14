import json
from functools import wraps

from flask import request, jsonify
from flask_jwt_extended import decode_token
from flask_jwt_extended.exceptions import JWTExtendedException
from flask_socketio import emit, join_room, leave_room, send
from sqlalchemy import func, distinct

from .models import Chat, Message
from .. import socketio, db
from ..users.models import User


def auth_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        token = request.headers['Authorization'].split()[1]
        user_id = None

        try:
            user_id = decode_token(token)['user_id']
        except JWTExtendedException:
            emit("unauthorized", {"message": "Your token has expired"})

        print("Authenticated " + str(user_id))
        return f(*args, user_id=user_id, **kwargs)

    return decorated_function


@socketio.on('connect')
@auth_required
def on_connect(*args, **kwargs):
    user = User.query.get(kwargs['user_id'])
    join_room(user.id)

    user.is_connected = True
    db.session.commit()

    print(f'{request.sid} connected, id = {user.id}')


@socketio.on('send_message')
@auth_required
def send_message(*args, **kwargs):
    user = User.query.get(kwargs['user_id'])
    interlocutor = User.query.get(args[1])

    chat = Chat.query.join(Chat.users). \
        filter(User.id.in_([user.id, interlocutor.id])). \
        group_by(Chat). \
        having(func.count(distinct(User.id)) == 2).first()

    if chat is None:
        chat = Chat()
        chat.users.extend([user, interlocutor])
        db.session.add(chat)
        db.session.commit()

    message = Message(user_id=user.id, chat_id=chat.id, body=args[0])
    chat.messages.append(message)
    db.session.commit()

    send(json.dumps(message.to_dict(), default=str), to=interlocutor.id)

    print(f'Sent new message "{args[0]}" from {user.id} to {interlocutor.id}')


@socketio.on('disconnect')
@auth_required
def on_disconnect(*args, **kwargs):
    user = User.query.get(kwargs['user_id'])
    leave_room(user.id)

    user.is_connected = False
    db.session.commit()

    print(f'{request.sid} disconnected, id = {user.id}')
