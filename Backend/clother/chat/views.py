from flask import Blueprint, jsonify, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from sqlalchemy import func, distinct

from .models import Chat, Message
from ..users.models import User
from ..utils import base_prefix, default_chat_page_size

blueprint = Blueprint('chats', __name__, url_prefix=(base_prefix + '/chats'))


@blueprint.route('')
@jwt_required()
def get_chats():
    user = User.query.get(get_jwt_identity())
    chats = user.chats
    chats.sort(key=lambda x: x.messages[-1].created_at, reverse=True)

    return jsonify([chat.to_dict(user_id_to=user.id) for chat in chats])


@blueprint.route('/<int:interlocutor_id>')
@jwt_required()
def get_messages(interlocutor_id):
    after = request.args.get('after', default=None, type=int)
    before = request.args.get('before', default=None, type=int)
    limit = request.args.get('limit', default=default_chat_page_size, type=int)

    user_id = get_jwt_identity()
    chat = Chat.query.join(Chat.users).\
        filter(User.id.in_([user_id, interlocutor_id])).\
        group_by(Chat).\
        having(func.count(distinct(User.id)) == 2).first()

    if not chat:
        return jsonify([])

    if after is None and before is None:  # initial request
        messages = chat.messages.order_by(Message.id.desc()).limit(limit).all()

    elif before is None:  # append
        messages = chat.messages.order_by(Message.id.desc()).\
            filter(Message.id < after).\
            limit(limit).all()

    else:  # prepend
        messages = chat.messages.order_by(Message.id.asc()).filter(Message.id > before).limit(limit).all()
        messages.reverse()

    return jsonify([message.to_dict() for message in messages])
