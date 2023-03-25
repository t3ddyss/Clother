import json
from http import HTTPStatus

from flask import Blueprint, jsonify, request
from flask_jwt_extended import jwt_required, get_jwt_identity

from .models import User, UserImage
from .. import db
from ..auth.error import AuthError
from ..auth.validators import validate_name, validate_status
from clother.common.constants import BASE_PREFIX
from ..common.error import CommonError
from ..images.utils import is_allowed_image, store_images

blueprint = Blueprint('users', __name__, url_prefix=(BASE_PREFIX + '/users'))


@blueprint.get('/<int:user_id>')
@jwt_required()
def get_user(user_id):
    user = User.query.get(user_id)
    if user is None:
        return jsonify(AuthError.USER_NOT_FOUND.to_dict()), HTTPStatus.NOT_FOUND
    else:
        return jsonify(user.to_details_dict())


@blueprint.post('/update')
@jwt_required()
def update_user():
    user = User.query.get(get_jwt_identity())
    data = json.loads(request.form['request'])

    name = data['name']
    status = data['status']
    image = next(iter(request.files.getlist('file') or []), None)

    if not validate_name(name):
        return jsonify(AuthError.INVALID_NAME.to_dict()), HTTPStatus.UNPROCESSABLE_ENTITY
    if status and not validate_status(status):
        return jsonify(AuthError.INVALID_STATUS.to_dict()), HTTPStatus.UNPROCESSABLE_ENTITY
    if image and not is_allowed_image(image.filename):
        return jsonify(CommonError.UNSUPPORTED_FILE_TYPE), HTTPStatus.BAD_REQUEST

    user.name = name
    user.status = status
    if image:
        uri = store_images([image])[0]
        user.image = UserImage(uri=uri)
    elif user.image:
        db.session.delete(user.image)
    db.session.commit()

    return jsonify(user.to_details_dict())
