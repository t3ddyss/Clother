import re
from threading import Thread
from flask import current_app
from flask_mail import Message
from .extensions import mail

base_prefix = '/api'
response_delay = 1.5
allowed_extensions = {'png', 'jpg', 'jpeg'}


def send_email_async(app, message):
    with app.app_context():
        mail.send(message)


def send_email(subject, recipients, html):
    message = Message(subject=subject, recipients=recipients, html=html)
    return Thread(target=send_email_async, args=(current_app._get_current_object(), message)).start()  # noqa


def validate_password(password):
    return get_password_regex().match(password)


# [8; 25] characters, at least 1 digit, at least 1 lowercase letter, at least 1 uppercase letter, no whitespaces,
# at least 1 special character
def get_password_regex():
    return re.compile(r'^(?=\S{8,25}$)(?=.*?\d)(?=.*?[a-z])(?=.*?[A-Z])(?=\S+$)(?=.*?[^A-Za-z\s0-9])')


def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in allowed_extensions

