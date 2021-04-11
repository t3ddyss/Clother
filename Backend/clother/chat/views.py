from flask import Blueprint
from flask_jwt_extended import jwt_required, get_jwt_identity

from .models import Chat, Message
from .. import db
from ..users.models import User
from ..utils import base_prefix

blueprint = Blueprint('messages', __name__, url_prefix=(base_prefix + '/messages'))


@blueprint.route('')
@jwt_required()
def get_chats():
    user_id = get_jwt_identity()
    user = User.query.get(user_id)
    chats = db.session.query(User, Chat, Message).\
        filter(user.in_(Chat.users)).\
        order_by(Message.created_at.desc)

    debug = 0
    return "Success"
