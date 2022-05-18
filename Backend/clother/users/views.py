import json
import time

from flask import Blueprint, jsonify, request
from flask_jwt_extended import jwt_required, get_jwt_identity

from .models import User, UserImage
from .. import db
from ..auth.utils import validate_name
from ..constants import BASE_PREFIX, RESPONSE_DELAY
from ..images.utils import is_allowed_image, store_images

blueprint = Blueprint('users', __name__, url_prefix=(BASE_PREFIX + '/users'))


@blueprint.get('/<int:user_id>')
@jwt_required()
def get_user(user_id):
    user = User.query.get(user_id)
    if user is None:
        return {'message': "User doesn't exist"}, 404
    else:
        return jsonify(user.to_details_dict(request.url_root))


@blueprint.post('/update')
@jwt_required()
def update_user():
    user = User.query.get(get_jwt_identity())
    data = json.loads(request.form['request'])

    name = data['name']
    status = data['status']
    image = next(iter(request.files.getlist('file') or []), None)

    if not validate_name(name):
        return {'message': "Name should contain at least 2 and less than 50 characters"}, 422
    if status and len(status) > 70:
        return {'message': "Status is limited to 70 symbols"}, 422
    if image and not is_allowed_image(image.filename):
        return {"message": "This file type is not allowed"}, 400

    user.name = name
    user.status = status
    if image:
        uri = store_images([image])[0]
        user.image = UserImage(uri=uri)
    else:
        db.session.delete(user.image)
    db.session.commit()

    return jsonify(user.to_details_dict(request.url_root))


# Simulate response delay while testing app on localhost
@blueprint.before_request
def simulate_delay():
    time.sleep(RESPONSE_DELAY)
