import math

import eventlet
from eventlet import wsgi
from flask import Flask
from sqlalchemy import event

from clother.extensions import db, migrate, jwt, mail, socketio
from clother import admin, authentication, users, offers, images, chat


def create_app(config_filename):
    app = Flask(__name__, instance_relative_config=True)
    app.config.from_pyfile(f"../{config_filename}")
    app.config.from_pyfile(config_filename)

    register_extensions(app)
    register_blueprints(app)

    # TODO database event doesn't work with eventlet
    wsgi.server(eventlet.listen(('', 5000)), app)

    with app.app_context():
        @event.listens_for(db.engine, 'connect')
        def on_connect(dbapi_con, connection_record):
            dbapi_con.create_function('sin', 1, math.sin)
            dbapi_con.create_function('cos', 1, math.cos)
            dbapi_con.create_function('acos', 1, math.acos)
            dbapi_con.create_function('radians', 1, math.radians)

    return app


def register_extensions(app):
    db.init_app(app)
    migrate.init_app(app, db)
    jwt.init_app(app)
    mail.init_app(app)
    socketio.init_app(app)


def register_blueprints(app):
    app.register_blueprint(admin.commands.blueprint)
    app.register_blueprint(authentication.views.blueprint)
    app.register_blueprint(users.views.blueprint)
    app.register_blueprint(offers.views.blueprint)
    app.register_blueprint(images.views.blueprint)
    app.register_blueprint(chat.views.blueprint)
