import asyncio
import json
from http import HTTPStatus

from flask import Blueprint, jsonify, request, current_app, abort
from flask_jwt_extended import jwt_required, get_jwt_identity
from sqlalchemy import func, distinct

from .error import ChatError
from .events import SocketEvent
from .fcm import send_data_message
from .models import Chat, Message, MessageImage
from .. import db, socketio
from ..common.error import CommonError
from ..images.utils import is_allowed_image, store_images
from ..users.models import User
from clother.common.constants import BASE_PREFIX

blueprint = Blueprint('chats', __name__, url_prefix=(BASE_PREFIX + '/chats'))
DEFAULT_CHAT_PAGE_SIZE = 25


@blueprint.get('')
@jwt_required()
def get_chats():
    user = User.query.get(get_jwt_identity())

    # TODO use query
    chats = user.chats
    chats.sort(key=lambda x: x.messages[-1].created_at, reverse=True)

    return jsonify([chat.to_dict(addressee_id=user.id) for chat in chats])


@blueprint.get('/<int:interlocutor_id>')
@jwt_required()
def get_messages(interlocutor_id):
    after = request.args.get('after', default=None, type=int)
    before = request.args.get('before', default=None, type=int)
    limit = request.args.get('limit', default=DEFAULT_CHAT_PAGE_SIZE, type=int)

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
        return jsonify(ChatError.IMAGE_LIMIT_EXCEEDED.to_dict()), HTTPStatus.BAD_REQUEST
    if any(not (file and is_allowed_image(file.filename)) for file in files):
        return jsonify(CommonError.UNSUPPORTED_FILE_TYPE.to_dict()), HTTPStatus.BAD_REQUEST
    uris = store_images(files)
    for uri in uris:
        message.images.append(MessageImage(uri=uri))

    chat.messages.append(message)
    db.session.commit()

    chat_dict = chat.to_dict(addressee_id=interlocutor.id)
    message_dict = message.to_dict()

    if not is_existing_chat:
        socketio.emit(SocketEvent.NEW_CHAT, json.dumps(chat_dict), to=interlocutor.id)
        asyncio.create_task(send_fcm_event_if_needed({'chat': chat_dict}, interlocutor))
    else:
        socketio.emit(SocketEvent.NEW_MESSAGE, json.dumps(message_dict), to=interlocutor.id)
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
    chat = Chat.query.get(message.chat_id)
    interlocutor_id = next(x for x in chat.users if x.id != user.id).id

    if message.user_id == user.id:
        db.session.delete(message)
        db.session.commit()
        socketio.emit(SocketEvent.DELETE_MESSAGE, json.dumps(message.to_dict()), to=interlocutor_id)
        return {}
    else:
        abort(HTTPStatus.FORBIDDEN)


async def send_fcm_event_if_needed(payload: dict, interlocutor):
    if interlocutor.device_token and not interlocutor.is_online:
        send_data_message(current_app.config['FCM_API_KEY'], interlocutor.device_token, payload)
