import eventlet
from eventlet import wsgi
from flask import Flask

from clother.extensions import db, migrate, jwt, mail, socketio
from clother import admin, authentication, users, offers, images, chat


def create_app(config_file='config.py'):
    app = Flask(__name__, instance_relative_config=True)
    app.config.from_pyfile(f"../{config_file}")
    app.config.from_pyfile(config_file)

    register_extensions(app)
    register_blueprints(app)

    # Uncomment this line if you are going to run this server without gunicorn (e.g. using flask run)
    # wsgi.server(eventlet.listen(('', 5000)), app)

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
