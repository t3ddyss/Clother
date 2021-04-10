import re
import time
from datetime import datetime
from datetime import timezone

from email_validator import validate_email, EmailNotValidError
from flask import Blueprint, current_app, request, url_for, render_template, after_this_request
from flask_jwt_extended import (create_access_token, create_refresh_token, get_jwt_identity,
                                jwt_required, get_jwt)
from itsdangerous import URLSafeTimedSerializer
from itsdangerous.exc import BadSignature, SignatureExpired, BadData
from sqlalchemy.exc import IntegrityError

from clother import db, jwt
from clother.users.models import User
from clother.utils import send_email, validate_password, response_delay, base_prefix
from .forms import ResetPasswordForm
from .models import TokenBlocklist

blueprint = Blueprint('auth', __name__, url_prefix=(base_prefix + '/auth'))


@jwt.token_in_blocklist_loader
def check_if_token_revoked(jwt_header, jwt_payload):
    jti = jwt_payload["jti"]
    token = db.session.query(TokenBlocklist.id).filter_by(jti=jti).scalar()
    return token is not None


@blueprint.route('/refresh')
@jwt_required(refresh=True)
def refresh_tokens():
    user_id = get_jwt_identity()

    jti = get_jwt()["jti"]
    now = datetime.now(timezone.utc)
    db.session.add(TokenBlocklist(jti=jti, created_at=now))
    db.session.commit()

    additional_claims = {"user_id": user_id}
    return {'user_id': user_id,
            'access_token': create_access_token(user_id, additional_claims=additional_claims),
            'refresh_token': create_refresh_token(user_id, additional_claims=additional_claims)
            }


@blueprint.route('/register', methods=['POST'])
def register():
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

    send_email(subject='Confirm your email address', recipients=[user.email], html=html)

    return {'message': 'Check your inbox'}


@blueprint.route('/confirm_email/<token>')
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
        additional_claims = {"user_id": user.id}
        return {'user_id': user.id,
                'access_token': create_access_token(user.id, additional_claims=additional_claims),
                'refresh_token': create_refresh_token(user.id, additional_claims=additional_claims)
                }
    else:
        return {"message": "Wrong email or password"}, 403


@blueprint.route('/forgot_password', methods=['POST'])
def forgot_password():
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

    send_email(subject='Forgot your password?', recipients=[user.email], html=html)

    return {'message': 'Check your inbox'}


@blueprint.route('/reset_password/<token>', methods=['GET', 'POST'])
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
        return 'Password has been successfully changed!'

    if not user.password == data['password']:
        return 'Password has already been reset', 410

    return render_template('reset_password.html', form=form)


# Simulate response delay while testing app on localhost
@blueprint.before_request
def simulate_delay():
    time.sleep(response_delay)
