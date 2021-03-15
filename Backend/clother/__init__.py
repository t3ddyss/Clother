from flask import Flask
from clother.extensions import db, migrate, cache, jwt, mail
from clother import admin, authentication, users, offers


def create_app(config_filename):
    app = Flask(__name__, instance_relative_config=True)
    app.config.from_pyfile(f"../{config_filename}")
    app.config.from_pyfile(config_filename)

    register_extensions(app)
    register_blueprints(app)
    return app


def register_extensions(app):
    db.init_app(app)
    migrate.init_app(app, db)
    # cache.init_app(app)
    jwt.init_app(app)
    mail.init_app(app)


def register_blueprints(app):
    app.register_blueprint(authentication.views.blueprint)
    app.register_blueprint(users.views.blueprint)
    app.register_blueprint(admin.commands.blueprint)
    app.register_blueprint(offers.views.blueprint)
