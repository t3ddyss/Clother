from enum import StrEnum, auto
from functools import wraps

from flask import request
from flask_jwt_extended import decode_token
from flask_jwt_extended.exceptions import JWTExtendedException
from flask_socketio import emit, join_room, leave_room
from jwt import ExpiredSignatureError

from .. import socketio, db
from ..users.models import User


class SocketEvent(StrEnum):
    CONNECT = auto()
    DISCONNECT = auto()
    NEW_CHAT = auto()
    NEW_MESSAGE = auto()
    DELETE_MESSAGE = auto()


def auth_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        token = request.headers['Authorization'].split()[1]
        allow_expired = f.__name__ == 'on_disconnect'

        try:
            user_id = decode_token(token, allow_expired=allow_expired)['user_id']
            return f(*args, user_id=user_id, **kwargs)
        except (JWTExtendedException, ExpiredSignatureError) as ex:
            emit('disconnect', {'message': 'Your token has expired'})
            return

    return decorated_function


@socketio.on(SocketEvent.CONNECT)
@auth_required
def on_connect(*args, **kwargs):
    user = User.query.get(kwargs['user_id'])
    join_room(user.id)

    user.is_online = True
    db.session.commit()

    print(f'{user.name} connected, id = {user.id}, sid = {request.sid}')


@socketio.on(SocketEvent.DISCONNECT)
@auth_required
def on_disconnect(*args, **kwargs):
    user = User.query.get(kwargs['user_id'])
    leave_room(user.id)

    user.is_online = False
    db.session.commit()

    print(f'{user.name} disconnected, id = {user.id}, sid = {request.sid}')
