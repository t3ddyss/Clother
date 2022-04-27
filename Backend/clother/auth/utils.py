import re

from flask import current_app
from flask_mail import Message
from clother import mail


async def send_email(subject, recipients, html):
    message = Message(subject=subject, recipients=recipients, html=html)
    with current_app.app_context():
        mail.send(message)


def validate_password(password):
    return get_password_regex().match(password)


# [8; 25] characters, at least 1 digit, at least 1 lowercase letter, at least 1 uppercase letter, no whitespaces,
# at least 1 special character
def get_password_regex():
    return re.compile(r'^(?=\S{8,25}$)(?=.*?\d)(?=.*?[a-z])(?=.*?[A-Z])(?=\S+$)(?=.*?[^A-Za-z\s0-9])')
