from threading import Thread
from flask import current_app
from flask_mail import Message
from .extensions import mail


def send_email_async(app, message):
    with app.app_context():
        mail.send(message)


def send_email(subject, recipients, html):
    message = Message(subject=subject, recipients=recipients, html=html)
    return Thread(target=send_email_async, args=(current_app._get_current_object(), message)).start()  # noqa
