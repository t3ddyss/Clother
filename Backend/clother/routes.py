import time
import re
import asyncio

from clother import app, db, jwt, mail
from clother.models import User

from flask import jsonify, request, url_for, abort, render_template
from werkzeug.security import generate_password_hash, check_password_hash
from flask_jwt_extended import (create_access_token, create_refresh_token,
                                get_jwt_identity, jwt_required)
from email_validator import validate_email, EmailNotValidError
from sqlalchemy.exc import IntegrityError
from .util.security import ts
from flask_mail import Message


async def send_message(message):
    mail.send(message)


@app.route('/register', methods=['POST'])
def register():
    if not request.is_json:
        return jsonify({'message': 'Expected JSON in the request body'}), 400
    data = request.get_json()

    email = data['email']
    try:
        valid_email = validate_email(email).email
    except EmailNotValidError:
        return jsonify({'message': 'Email address is invalid'}), 422

    password = data['password']
    password_pattern = re.compile('^'
                                  '(?=\S{8,25}$)'  # [8; 25] characters
                                  '(?=.*?\d)'  # at least 1 digit
                                  '(?=.*?[a-z])'  # at least 1 lowercase letter
                                  '(?=.*?[A-Z])'  # at least 1 uppercase letter
                                  '(?=\\S+$)'  # no whitespaces
                                  '(?=.*?[^A-Za-z\s0-9])')  # at least 1 special character
    if not password_pattern.match(password):
        return jsonify({'message': 'Password should be at least 8 and less than 25 characters, '
                                   'contain at least 1 digit, 1 upcase letter, 1 lowercase letter and '
                                   '1 special character'}), 422

    name = data['name']
    name_pattern = re.compile('^(?=\S{2,50}$)')
    if not name_pattern.match(name):
        return jsonify({'message': 'Name should be at least 2 and less than 50 characters'}), 422

    hashed_password = generate_password_hash(data['password'], method='sha256')
    new_user = User(email=valid_email, password=hashed_password, name=name)

    try:
        db.session.add(new_user)
        db.session.commit()
    except IntegrityError:
        db.session.rollback()
        return jsonify({'message': 'User with this email is already exists'}), 403

    user = User.query.filter_by(email=new_user.email).first()

    token = ts.dumps(user.email, salt='confirm_email')
    confirm_url = url_for(
        'confirm_email',
        token=token,
        _external=True)

    html = render_template(
        'activate_account.html',
        action_url=confirm_url)
    message = Message('Confirm your email address', sender='fnsokolov@edu.hse.ru', recipients=[user.email], html=html)
    asyncio.run(send_message(message))

    return jsonify({'access_token': create_access_token(user.id),
                    'refresh_token': create_refresh_token(user.id)
                    })


@app.route('/confirm/<token>')
def confirm_email(token):
    email = None
    try:
        email = ts.loads(token, salt="confirm_email", max_age=86400)
    except:
        abort(404)

    user = User.query.filter_by(email=email).first_or_404()

    user.email_verified = True

    # db.session.add(user)
    db.session.commit()

    return jsonify({'message': 'Your account was successfully activated!'})


@app.route('/login', methods=['POST'])
def login():
    if not request.is_json:
        return jsonify({"message": "Missing JSON in request"}), 400

    email = request.json.get('email', None)
    password = request.json.get('password', None)

    if not email:
        return jsonify({"message": "Missing email parameter"}), 400
    if not password:
        return jsonify({"message": "Missing password parameter"}), 400

    if email != 'test' or password != 'test':
        return jsonify({"message": "Bad email or password"}), 401

    # Identity can be any data that is json serializable
    access_token = create_access_token(identity=email)
    return jsonify(access_token=access_token), 200


@app.route('/users', methods=['GET'])
def get_users():
    users = User.query.all()
    result = []

    for user in users:
        result.append(user.to_json())
    return jsonify(result)


@app.route('/users/<user_id>', methods=['GET'])
def get_user(user_id):
    user = User.query.filter_by(id=user_id).first()

    if not user:
        return jsonify({"message": f"User with ID {user_id} doesn't exist"}), 404
    else:
        return jsonify(user.to_json())


@app.route('/users_test')
def get_users_test():
    time.sleep(0.5)
    return jsonify([dict(name="User1", email="user1@example.com"),
                    dict(name="User2", email="user2@example.com")])


@app.route('/error_test')
def get_error_test():
    time.sleep(0.5)
    return jsonify({'message': 'Some error'}), 404


@app.route('/')
def hello_world():
    return 'Hello World!'
