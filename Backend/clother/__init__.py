from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from flask_jwt_extended import JWTManager
from flask_mail import Mail, Message

from config import Config

app = Flask(__name__, instance_relative_config=True)
app.config.from_object(Config)
app.config.from_pyfile('config.cfg')

jwt = JWTManager(app)
db = SQLAlchemy(app)
mail = Mail(app)

from clother import routes
