from flask import Blueprint
from .models import User
from flask_jwt_extended import jwt_required

blueprint = Blueprint('users', __name__)


@blueprint.route('/users/<user_id>')
@jwt_required()
def get_user(user_id):
    user = User.query.filter_by(id=user_id).first()

    if not user:
        return {"message": f"User with ID {user_id} doesn't exist"}, 404
    else:
        return user.to_dict()
