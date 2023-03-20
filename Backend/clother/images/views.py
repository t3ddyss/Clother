import os
from http import HTTPStatus

from flask import Blueprint, abort, send_file, current_app

from clother.common.constants import BASE_PREFIX

blueprint = Blueprint('images', __name__, url_prefix=(BASE_PREFIX + '/images'))


@blueprint.get('/<filename>')
def get_image(filename):
    try:
        extension = filename.split('.')[-1]
        return send_file(os.path.join(current_app.config['UPLOAD_FOLDER'], filename),
                         mimetype=f'image/{extension}')
    except FileNotFoundError:
        abort(HTTPStatus.NOT_FOUND)
