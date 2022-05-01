import os
import time

from flask import Blueprint, abort, send_file, current_app

from clother.constants import BASE_PREFIX, RESPONSE_DELAY

blueprint = Blueprint('images', __name__, url_prefix=(BASE_PREFIX + '/images'))


@blueprint.get('/<filename>')
def get_image(filename):
    try:
        extension = filename.split('.')[-1]
        return send_file(os.path.join(current_app.config['UPLOAD_FOLDER'], filename),
                         mimetype=f'image/{extension}')
    except FileNotFoundError:
        abort(404)


# Simulate response delay while testing app on localhost
@blueprint.before_request
def simulate_delay():
    time.sleep(RESPONSE_DELAY)