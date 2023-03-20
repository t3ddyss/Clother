import time
from http import HTTPStatus

from flask import request, jsonify, Blueprint

from clother.common.error import CommonError

blueprint = Blueprint('other', __name__)
RESPONSE_DELAY_SECONDS = 1.5


@blueprint.before_app_request
def execute_before_each_request():
    time.sleep(RESPONSE_DELAY_SECONDS)  # Simulate response delay for testing
    if not request.form and request.mimetype and not request.is_json:
        return jsonify(CommonError.UNSUPPORTED_MIME_TYPE.to_dict()), HTTPStatus.UNSUPPORTED_MEDIA_TYPE
