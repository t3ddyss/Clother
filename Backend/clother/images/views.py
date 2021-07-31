import os

from flask import Blueprint, abort, send_file, current_app

from clother.utils import base_prefix

blueprint = Blueprint('images', __name__, url_prefix=(base_prefix + '/images'))


@blueprint.get('/<filename>')
def get_image(filename):
    try:
        extension = filename.split('.')[-1]
        return send_file(os.path.join(current_app.config['UPLOAD_FOLDER'], filename),
                         mimetype=f'image/{extension}')
    except FileNotFoundError:
        abort(404)
