import asyncio
import json
import time

from flask import Blueprint, jsonify, request, current_app, abort
from flask_jwt_extended import jwt_required, get_jwt_identity
from sqlalchemy import func, distinct

from .fcm import send_data_message
from .models import Chat, Message, MessageImage
from .. import db, socketio
from ..images.utils import is_allowed_image, store_images
from ..users.models import User
from ..constants import BASE_PREFIX, DEFAULT_MESSAGES_PAGE_SIZE, RESPONSE_DELAY

blueprint = Blueprint('chats', __name__, url_prefix=(BASE_PREFIX + '/chats'))


@blueprint.get('')
@jwt_required()
def get_chats():
    user = User.query.get(get_jwt_identity())
    chats = user.chats
    chats.sort(key=lambda x: x.messages[-1].created_at, reverse=True)

    return jsonify([chat.to_dict(url_root=request.url_root, addressee_id=user.id) for chat in chats])


@blueprint.get('/<int:interlocutor_id>')
@jwt_required()
def get_messages(interlocutor_id):
    after = request.args.get('after', default=None, type=int)
    before = request.args.get('before', default=None, type=int)
    limit = request.args.get('limit', default=DEFAULT_MESSAGES_PAGE_SIZE, type=int)

    user_id = get_jwt_identity()
    chat = Chat.query.join(Chat.users). \
        filter(User.id.in_([user_id, interlocutor_id])). \
        group_by(Chat). \
        having(func.count(distinct(User.id)) == 2).first()

    if not chat:
        return jsonify([])

    if after is None and before is None:  # initial request
        messages = chat.messages.order_by(Message.id.desc()).limit(limit).all()

    elif before is None:  # append
        messages = chat.messages.order_by(Message.id.desc()). \
            filter(Message.id < after). \
            limit(limit).all()

    else:  # prepend
        messages = chat.messages.order_by(Message.id.asc()).filter(Message.id > before).limit(limit).all()
        messages.reverse()

    return jsonify([message.to_dict(url_root=request.url_root) for message in messages])


@blueprint.post('/message')
@jwt_required()
async def send_message():
    data = json.loads(request.form['request'])
    sender = User.query.get(get_jwt_identity())
    interlocutor = User.query.get(request.args.get('to', default=None, type=int))

    chat = Chat.query.join(Chat.users). \
        filter(User.id.in_([sender.id, interlocutor.id])). \
        group_by(Chat). \
        having(func.count(distinct(User.id)) == 2).first()

    is_existing_chat = chat is not None
    if not is_existing_chat:
        chat = Chat()
        chat.users.extend([sender, interlocutor])
        db.session.add(chat)
        db.session.commit()

    message = Message(user_id=sender.id, chat_id=chat.id, body=data.get('body'))

    files = request.files.getlist('file')
    if len(files) > 5:
        return {"message": "You cannot upload more than 5 images"}, 400
    for file in files:
        if not (file and is_allowed_image(file.filename)):
            return {"message": "This file type is not allowed"}, 400
    uris = store_images(files)
    for uri in uris:
        message.images.append(MessageImage(uri=uri))

    chat.messages.append(message)
    db.session.commit()

    chat_dict = chat.to_dict(url_root=request.url_root, addressee_id=interlocutor.id)
    message_dict = message.to_dict(url_root=request.url_root)

    if not is_existing_chat:
        socketio.emit('chat', json.dumps(chat_dict), to=interlocutor.id)
        asyncio.create_task(send_fcm_event_if_needed({'chat': chat_dict}, interlocutor))
    else:
        socketio.send(json.dumps(message_dict), to=interlocutor.id)
        asyncio.create_task(send_fcm_event_if_needed({'message': message_dict}, interlocutor))

    if request.args.get('return_chat', default=False, type=json.loads):
        return jsonify(chat_dict)
    else:
        return jsonify(message_dict)


@blueprint.delete('/message/<int:message_id>')
@jwt_required()
def delete_message(message_id):
    user = User.query.get(get_jwt_identity())
    message = Message.query.get(message_id)

    if message.user_id == user.id:
        db.session.delete(message)
        db.session.commit()
        return {"message": "Message was successfully deleted"}
    else:
        abort(403)


async def send_fcm_event_if_needed(payload: dict, interlocutor):
    if interlocutor.device_token and not interlocutor.is_connected:
        send_data_message(current_app.config['FCM_API_KEY'], interlocutor.device_token, payload)


# Simulate response delay while testing app on localhost
@blueprint.before_request
def simulate_delay():
    time.sleep(RESPONSE_DELAY)
