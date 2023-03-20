import asyncio
import datetime
from http import HTTPStatus

from email_validator import validate_email, EmailNotValidError
from flask import Blueprint, current_app, request, url_for, render_template, after_this_request, jsonify
from flask_jwt_extended import (create_access_token, create_refresh_token, get_jwt_identity,
                                jwt_required, get_jwt)
from itsdangerous import URLSafeTimedSerializer
from itsdangerous.exc import BadSignature, SignatureExpired
from sqlalchemy.exc import IntegrityError

from clother import db, jwt
from clother.common.constants import BASE_PREFIX
from clother.users.models import User
from .error import AuthError
from .forms import ResetPasswordForm
from .mail import send_email
from .models import TokenBlocklist
from .validators import validate_password, validate_name
from ..common.error import CommonError

blueprint = Blueprint('auth', __name__, url_prefix=(BASE_PREFIX + '/auth'))
CONFIRM_EMAIL_SALT = 'confirm_email'
CONFIRM_EMAIL_TOKEN_MAX_AGE = datetime.timedelta(days=30)
RESET_PASSWORD_SALT = 'reset_password'
RESET_PASSWORD_TOKEN_MAX_AGE = datetime.timedelta(minutes=15)


@jwt.token_in_blocklist_loader
def check_if_token_revoked(jwt_header, jwt_payload):
    jti = jwt_payload['jti']
    token = db.session.query(TokenBlocklist.id).filter_by(jti=jti).scalar()
    return token is not None


@blueprint.get('/refresh')
@jwt_required(refresh=True)
def refresh_tokens():
    user_id = get_jwt_identity()
    user = User.query.get(user_id)

    jti = get_jwt()['jti']
    try:
        db.session.add(TokenBlocklist(jti=jti))
        db.session.commit()
    except IntegrityError:
        return jsonify(AuthError.INVALID_TOKEN.to_dict()), HTTPStatus.FORBIDDEN

    additional_claims = {'user_id': user_id}
    return {'user': user.to_dict(request.url_root),
            'access_token': create_access_token(user_id, additional_claims=additional_claims),
            'refresh_token': create_refresh_token(user_id, additional_claims=additional_claims)}


@blueprint.post('/device/<token>')
@jwt_required()
def register_device(token):
    user = User.query.get(get_jwt_identity())
    user.device_token = token
    db.session.commit()
    return {}


@blueprint.post('/register')
async def register():
    data = request.get_json()

    try:
        email = data['email']
        password = data['password']
        name = data['name']
    except KeyError:
        return jsonify(CommonError.MISSING_MANDATORY_PARAMETER.to_dict()), HTTPStatus.BAD_REQUEST

    try:
        valid_email = validate_email(email).email
    except EmailNotValidError:
        return jsonify(AuthError.INVALID_EMAIL.to_dict()), HTTPStatus.UNPROCESSABLE_ENTITY

    if not validate_password(password):
        return jsonify(AuthError.INVALID_PASSWORD.to_dict()), HTTPStatus.UNPROCESSABLE_ENTITY

    if not validate_name(name):
        return jsonify(AuthError.INVALID_NAME.to_dict()), HTTPStatus.UNPROCESSABLE_ENTITY

    user = User(email=valid_email, name=name)
    user.set_password(password)

    try:
        db.session.add(user)
        db.session.commit()
    except IntegrityError:
        db.session.rollback()
        return jsonify(AuthError.EMAIL_OCCUPIED.to_dict()), HTTPStatus.CONFLICT

    ts = URLSafeTimedSerializer(current_app.config['SECRET_KEY'])
    token = ts.dumps(user.id, salt=CONFIRM_EMAIL_SALT)
    confirmation_url = url_for(
        'auth.confirm_email',
        token=token,
        _external=True)
    html = render_template(
        'confirmation.html',
        subject_text="You are almost done!",
        body_text="To complete email verification, please press the button below",
        button_text="Verify email",
        action_url=confirmation_url)
    asyncio.create_task(send_email(subject="Confirm your email address", recipients=[user.email], html=html))

    return {}


@blueprint.get('/confirm_email/<token>')
def confirm_email(token):
    ts = URLSafeTimedSerializer(current_app.config['SECRET_KEY'])
    try:
        user_id = ts.loads(token, salt=CONFIRM_EMAIL_SALT, max_age=CONFIRM_EMAIL_TOKEN_MAX_AGE.seconds)
    except SignatureExpired:
        return jsonify(AuthError.TOKEN_EXPIRED.to_dict()), HTTPStatus.FORBIDDEN
    except BadSignature:
        return jsonify(AuthError.INVALID_TOKEN.to_dict()), HTTPStatus.FORBIDDEN

    user = User.query.get(user_id)
    user.email_verified = True
    db.session.commit()

    return {}


@blueprint.post('/login')
def login():
    data = request.get_json()
    try:
        email = data['email']
        password = data['password']
    except KeyError:
        return jsonify(CommonError.MISSING_MANDATORY_PARAMETER.to_dict()), HTTPStatus.BAD_REQUEST

    user = User.query.filter_by(email=email).first()
    if user and not user.email_verified:
        return jsonify(AuthError.EMAIL_NOT_VERIFIED.to_dict()), HTTPStatus.FORBIDDEN

    if user and user.check_password(password):
        additional_claims = {'user_id': user.id}
        return {'user': user.to_details_dict(request.url_root),
                'access_token': create_access_token(user.id, additional_claims=additional_claims),
                'refresh_token': create_refresh_token(user.id, additional_claims=additional_claims)}
    else:
        return jsonify(AuthError.INVALID_CREDENTIALS.to_dict()), HTTPStatus.FORBIDDEN


@blueprint.post('/forgot_password')
async def forgot_password():
    data = request.get_json()
    try:
        email = data['email']
    except KeyError:
        return jsonify(CommonError.MISSING_MANDATORY_PARAMETER.to_dict()), HTTPStatus.BAD_REQUEST

    try:
        valid_email = validate_email(email).email
    except EmailNotValidError:
        return jsonify(AuthError.INVALID_EMAIL.to_dict()), HTTPStatus.UNPROCESSABLE_ENTITY

    user = User.query.filter_by(email=valid_email).first()
    if not user:
        return jsonify(AuthError.USER_NOT_FOUND.to_dict()), HTTPStatus.UNPROCESSABLE_ENTITY

    ts = URLSafeTimedSerializer(current_app.config['SECRET_KEY'])
    token = ts.dumps(user.id, salt=RESET_PASSWORD_SALT)
    confirmation_url = url_for(
        'auth.reset_password',
        token=token,
        _external=True)

    html = render_template(
        'confirmation.html',
        subject_text="Password reset",
        body_text="To reset your password, please press the button below. If you didn't request a password reset, "
                  "just ignore this email",
        button_text="Reset password",
        action_url=confirmation_url)

    asyncio.create_task(send_email(subject="Forgot your password?", recipients=[user.email], html=html))

    return {}


@blueprint.route('/reset_password/<token>', methods=['GET', 'POST'])
def reset_password(token):
    @after_this_request
    def execute_after_request(response):
        response.headers['Cache-Control'] = 'no-cache, no-store, must-revalidate'
        return response

    ts = URLSafeTimedSerializer(current_app.config['SECRET_KEY'])
    try:
        user_id = ts.loads(token, salt=RESET_PASSWORD_SALT, max_age=RESET_PASSWORD_TOKEN_MAX_AGE.seconds)
    except SignatureExpired:
        return jsonify(AuthError.TOKEN_EXPIRED.to_dict()), HTTPStatus.FORBIDDEN
    except BadSignature:
        return jsonify(AuthError.INVALID_TOKEN.to_dict()), HTTPStatus.FORBIDDEN

    user = User.query.get(user_id)

    form = ResetPasswordForm()
    if form.validate_on_submit():
        user.set_password(form.password.data)
        db.session.commit()
        return {}

    return render_template('reset_password.html', form=form)
