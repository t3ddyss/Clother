import json
from functools import wraps
from time import sleep

from flask import request, current_app
from flask_jwt_extended import decode_token
from flask_jwt_extended.exceptions import JWTExtendedException
from flask_socketio import emit, join_room, leave_room, send
from pyfcm import FCMNotification
from sqlalchemy import func, distinct

from .models import Chat, Message
from .. import socketio, db
from ..users.models import User


def auth_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        token = request.headers['Authorization'].split()[1]
        allow_expired = f.__name__ == 'on_disconnect'

        try:
            user_id = decode_token(token, allow_expired=allow_expired)['user_id']
            print('Authenticated ' + str(user_id) + f' for {f.__name__}')
            return f(*args, user_id=user_id, **kwargs)
        except JWTExtendedException:
            emit('unauthorized', {'message': 'Your token has expired'})

    return decorated_function


@socketio.on('connect')
@auth_required
def on_connect(*args, **kwargs):
    user = User.query.get(kwargs['user_id'])
    join_room(user.id)

    user.is_connected = True
    db.session.commit()

    print(f'{request.sid} connected, id = {user.id}, name = {user.name}')


@socketio.on('send_message')
@auth_required
def send_message(*args, **kwargs):
    sleep(1)  # For demonstration purposes, remove later

    sender = User.query.get(kwargs['user_id'])
    interlocutor = User.query.get(args[1])

    chat = Chat.query.join(Chat.users). \
        filter(User.id.in_([sender.id, interlocutor.id])). \
        group_by(Chat). \
        having(func.count(distinct(User.id)) == 2).first()

    is_new_chat = chat is None

    if is_new_chat:
        chat = Chat()
        chat.users.extend([sender, interlocutor])
        db.session.add(chat)
        db.session.commit()

    message = json.loads(args[0])

    new_message = Message(user_id=sender.id, chat_id=chat.id, body=message['body'])
    chat.messages.append(new_message)
    db.session.commit()

    push_service = FCMNotification(api_key=current_app.config['FCM_API_KEY'])
    if interlocutor.device_token and not interlocutor.is_connected:
        push_service.notify_single_device(registration_id=interlocutor.device_token,
                                          message_title=sender.name,
                                          message_body=new_message.body,
                                          android_channel_id="Messages")

    if is_new_chat:
        emit("chat", json.dumps(chat.to_dict(user_id_to=interlocutor.id)), to=interlocutor.id)
        emit(f'message{message["local_id"]}', json.dumps(chat.to_dict(user_id_to=sender.id)))
    else:
        send(json.dumps(new_message.to_dict()), to=interlocutor.id)
        emit(f'message{message["local_id"]}', json.dumps(new_message.to_dict()))

    print(f'Sent new message "{new_message.body}" from {sender.name} to {interlocutor.name}')


@socketio.on('disconnect')
@auth_required
def on_disconnect(*args, **kwargs):
    user = User.query.get(kwargs['user_id'])
    leave_room(user.id)

    user.is_connected = False
    db.session.commit()

    print(f'{request.sid} disconnected, id = {user.id}, name = {user.name}')
