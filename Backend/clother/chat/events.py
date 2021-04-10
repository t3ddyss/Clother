import json
from functools import wraps

from flask import request, g
from flask_jwt_extended import jwt_required, decode_token
from flask_jwt_extended.exceptions import JWTExtendedException
from flask_socketio import emit, join_room, leave_room, send

from .. import socketio

rooms = dict()


def auth_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        token = request.headers['Authorization'].split()[1]
        user_id = None

        try:
            user_id = decode_token(token)['user_id']
        except JWTExtendedException:
            emit("unauthorized", {"message": "Your token has expired",
                                  "request_id": args[1]})

        print("Authenticated " + str(user_id))
        return f(*args, user_id=user_id, **kwargs)

    return decorated_function


@socketio.on('connect')
@auth_required
def on_connect(*args, **kwargs):
    user_id = kwargs['user_id']
    join_room(user_id)

    print(f'{request.sid} connected, id = {user_id}')


@socketio.on('new_message')
@auth_required
def send_message(*args, **kwargs):
    send(args[0], to=args[1])

    print(f'Sent new message "{args[0]}" to {args[1]}')


@socketio.on('disconnect')
@auth_required
def on_disconnect(*args, **kwargs):
    user_id = kwargs['user_id']
    leave_room(user_id)

    print(f'{request.sid} disconnected')

