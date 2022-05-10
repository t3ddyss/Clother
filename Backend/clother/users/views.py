import time

from flask import Blueprint, jsonify
from flask_jwt_extended import jwt_required

from .models import User
from ..constants import BASE_PREFIX, RESPONSE_DELAY

blueprint = Blueprint('users', __name__, url_prefix=(BASE_PREFIX + '/users'))


@blueprint.get('/<int:user_id>')
@jwt_required()
def get_user(user_id):
    user = User.query.get(user_id)
    if user is None:
        return {'message': "User doesn't exist"}, 404
    else:
        return jsonify(user.to_details_dict())


# Simulate response delay while testing app on localhost
@blueprint.before_request
def simulate_delay():
    time.sleep(RESPONSE_DELAY)