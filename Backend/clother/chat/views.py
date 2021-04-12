from flask import Blueprint, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity

from ..users.models import User
from ..utils import base_prefix

blueprint = Blueprint('messages', __name__, url_prefix=(base_prefix + '/chats'))


@blueprint.route('')
@jwt_required()
def get_chats():
    user = User.query.get(get_jwt_identity())
    chats = user.chats
    chats.sort(key=lambda x: x.messages[-1].created_at, reverse=True)

    return jsonify([chat.to_dict(user_id_to=user.id) for chat in chats])
