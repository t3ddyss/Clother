import json

from flask import request
from flask_jwt_extended import jwt_required
from flask_socketio import emit

from .. import socketio

rooms = dict()


@socketio.on('connect')
def on_connect():
    print(f'{request.sid} connected')
    emit("connection", {"response": "Hello world!"})


@socketio.on('disconnect')
def on_disconnect():
    print(f'{request.sid} disconnected')
    rooms.pop(request.sid, None)


@socketio.on('join')
@jwt_required()
def on_join(data):
    message = json.loads(data)
    print(message['fruit'])
