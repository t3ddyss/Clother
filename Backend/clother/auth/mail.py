from flask import current_app
from flask_mail import Message
from clother import mail


async def send_email(subject, recipients, html):
    message = Message(subject=subject, recipients=recipients, html=html)
    with current_app.app_context():
        mail.send(message)
