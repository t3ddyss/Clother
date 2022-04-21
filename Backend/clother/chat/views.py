import asyncio
import json

from flask import Blueprint, jsonify, request, current_app
from flask_jwt_extended import jwt_required, get_jwt_identity
from sqlalchemy import func, distinct

from .fcm import send_data_message
from .models import Chat, Message
from .. import db, socketio
from ..users.models import User
from ..utils import base_prefix, default_chat_page_size, response_delay

blueprint = Blueprint('chats', __name__, url_prefix=(base_prefix + '/chats'))


@blueprint.get('')
@jwt_required()
def get_chats():
    user = User.query.get(get_jwt_identity())
    chats = user.chats
    chats.sort(key=lambda x: x.messages[-1].created_at, reverse=True)

    return jsonify([chat.to_dict(user_id_to=user.id) for chat in chats])


@blueprint.get('/<int:interlocutor_id>')
@jwt_required()
def get_messages(interlocutor_id):
    after = request.args.get('after', default=None, type=int)
    before = request.args.get('before', default=None, type=int)
    limit = request.args.get('limit', default=default_chat_page_size, type=int)

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

    return jsonify([message.to_dict() for message in messages])


@blueprint.post('/message')
@jwt_required()
async def send_message():
    await asyncio.sleep(response_delay)

    message = json.loads(request.json)
    sender = User.query.get(message['user_id'])
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

    new_message = Message(user_id=sender.id, chat_id=chat.id, body=message['body'])
    chat.messages.append(new_message)
    db.session.commit()

    chat_dict = chat.to_dict(user_id_to=interlocutor.id)
    message_dict = new_message.to_dict()

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


async def send_fcm_event_if_needed(payload: dict, interlocutor):
    if interlocutor.device_token and not interlocutor.is_connected:
        send_data_message(current_app.config['FCM_API_KEY'], interlocutor.device_token, payload)

