import re
import time

from flask import Blueprint, current_app, request, url_for, render_template, after_this_request
from flask_jwt_extended import create_access_token, create_refresh_token
from email_validator import validate_email, EmailNotValidError
from itsdangerous import URLSafeTimedSerializer
from itsdangerous.exc import BadSignature, SignatureExpired, BadData
from sqlalchemy.exc import IntegrityError

from clother import db
from clother.user.models import User
from clother.utils import send_email, validate_password, response_delay
from .forms import ResetPasswordForm

blueprint = Blueprint('auth', __name__)


@blueprint.route('/register', methods=['POST'])
def register():
    time.sleep(response_delay)
    if not request.is_json:
        return {'message': 'Expected JSON in the request body'}, 400
    data = request.get_json()

    email = data['email']
    try:
        valid_email = validate_email(email).email
    except EmailNotValidError:
        return {'message': 'Email address is invalid'}, 422

    password = data['password']
    if not validate_password(password):
        return {'message': 'Password should be at least 8 and less than 25 characters, '
                           'contain at least 1 digit, 1 uppercase letter, 1 lowercase letter and '
                           '1 special character'}, 422

    name = data['name']
    name_pattern = re.compile(r'^(?=\S{2,50}$)')
    if not name_pattern.match(name):
        return {'message': 'Name should contain at least 2 and less than 50 characters'}, 422

    user = User(email=valid_email, name=name)
    user.set_password(password)

    try:
        db.session.add(user)
        db.session.commit()
    except IntegrityError:
        db.session.rollback()
        return {'message': 'User with this email address is already exists'}, 403

    ts = URLSafeTimedSerializer(current_app.config['SECRET_KEY'])
    token = ts.dumps(user.email, salt='confirm_email')
    confirmation_url = url_for(
        'auth.confirm_email',
        token=token,
        _external=True)

    html = render_template(
        'confirmation.html',
        subject_text='You are almost done!',
        body_text='To complete email verification, please press the button below.',
        button_text='Verify email',
        action_url=confirmation_url)

    # send_email(subject='Confirm your email address', recipients=[user.email], html=html)

    return {'message': 'Check your inbox'}


@blueprint.route('/auth/confirm_email/<token>')
def confirm_email(token):
    ts = URLSafeTimedSerializer(current_app.config['SECRET_KEY'])
    try:
        email = ts.loads(token, salt="confirm_email", max_age=2_592_000)  # 30 days
    except SignatureExpired as ex:
        try:
            email = ts.load_payload(ex.payload)
            User.query.filter_by(email=email).first().delete()
            db.session.commit()
        except BadData:
            return {'message': "Invalid token"}, 403
        return {'message': "Token has expired"}, 403
    except BadSignature:
        return {'message': "Invalid token"}, 403

    user = User.query.filter_by(email=email).first()
    user.email_verified = True
    db.session.commit()

    return 'Your account was successfully activated!'


@blueprint.route('/login', methods=['POST'])
def login():
    time.sleep(response_delay)
    if not request.is_json:
        return {"message": "Missing JSON in request"}, 400

    email = request.json.get('email', None)
    password = request.json.get('password', None)

    if not email:
        return {"message": "Missing email parameter"}, 400
    if not password:
        return {"message": "Missing password parameter"}, 400

    user = User.query.filter_by(email=email).first()
    if not user.email_verified:
        return {"message": "You haven't verified your email address"}, 403
    if user and user.check_password(password):
        return {'access_token': create_access_token(user.id),
                'refresh_token': create_refresh_token(user.id)
                }
    else:
        return {"message": "Wrong email or password"}, 403


@blueprint.route('/auth/forgot_password', methods=['POST'])
def forgot_password():
    time.sleep(response_delay)
    if not request.is_json:
        return {"message": "Missing JSON in request"}, 400
    data = request.get_json()

    email = data['email']
    try:
        valid_email = validate_email(email).email
    except EmailNotValidError:
        return {'message': 'Email address is invalid'}, 422

    user = User.query.filter_by(email=valid_email).first()
    if not user:
        return {'message': "User with this email address doesn't exist"}, 422

    ts = URLSafeTimedSerializer(current_app.config['SECRET_KEY'])
    token = ts.dumps({'email': user.email, 'password': user.password}, salt='reset_password')
    confirmation_url = url_for(
        'auth.reset_password',
        token=token,
        _external=True)

    html = render_template(
        'confirmation.html',
        subject_text='Password reset',
        body_text="To reset your password, please press the button below. If you didn't request a password reset, "
                  "just ignore this email",
        button_text='Reset password',
        action_url=confirmation_url)

    # send_email(subject='Forgot your password?', recipients=[user.email], html=html)

    return {'message': 'Check your inbox'}


@blueprint.route('/auth/reset_password/<token>', methods=['GET', 'POST'])
def reset_password(token):
    @after_this_request
    def after_request(response):
        response.headers["Cache-Control"] = "no-cache, no-store, must-revalidate"
        return response

    form = ResetPasswordForm()

    ts = URLSafeTimedSerializer(current_app.config['SECRET_KEY'])
    try:
        data = ts.loads(token, salt="reset_password", max_age=900)  # 15 minutes
    except SignatureExpired:
        return {'message': "Token has expired"}, 403
    except BadSignature:
        return {'message': "Invalid token"}, 403

    user = User.query.filter_by(email=data['email']).first()

    if form.validate_on_submit():
        user.set_password(form.password.data)
        db.session.commit()
        return 'Password has been successfully changed'

    if not user.password == data['password']:
        return 'Password has already been reset', 410

    return render_template('reset_password.html', form=form)


@blueprint.route('/email_test')
def get_email_test():
    # html = render_template(
    #     'confirmation.html',
    #     subject_text='You are almost done!',
    #     body_text='To complete email verification, please press the button below.',
    #     button_text='Verify email',
    #     action_url='https://google.com')
    #
    # send_email(subject='Confirm your email address', recipients=['tedorsokolov@gmail.com'], html=html)
    html = render_template(
        'confirmation.html',
        subject_text='Password reset',
        body_text="To reset your password, please press the button below. If you didn't request a password reset, "
                  "just ignore this email",
        button_text='Reset password',
        action_url='https://google.com')

    send_email(subject='Forgot your password?', recipients=['tedorsokolov@gmail.com'], html=html)

    return {'message': 'Check your inbox'}


@blueprint.route('/error_test')
def get_error_test():
    time.sleep(0.5)
    return 'Some error', 404


@blueprint.route('/template_test')
def get_template_test():
    return render_template(
        'confirmation.html',
        subject_text='You are almost done!',
        body_text='To complete email verification, please press the button below.',
        button_text='Verify email',
        action_url='https://google.com')


@blueprint.route('/form_test', methods=['GET', 'POST'])
def get_form_test():
    form = ResetPasswordForm()

    if form.validate_on_submit():
        return 'Success'
    return render_template('reset_password.html', form=form)


@blueprint.route('/')
def hello_world():
    time.sleep(0.5)
    return 'Hello world!'
