import os
import secrets

from flask import current_app
from werkzeug.utils import secure_filename


def is_allowed_image(filename):
    return '.' in filename and filename.split('.')[-1].lower() in {'png', 'jpg', 'jpeg'}


def store_images(images: list) -> list:
    uris = []
    for image in images:
        filename = secrets.token_urlsafe(10) + secure_filename(image.filename)
        image.save(os.path.join(current_app.config['UPLOAD_FOLDER'], filename))
        uris.append(filename)
    return uris
