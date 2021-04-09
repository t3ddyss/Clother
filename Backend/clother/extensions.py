from flask_caching import Cache
from flask_jwt_extended import JWTManager
from flask_migrate import Migrate
from flask_socketio import SocketIO
from flask_sqlalchemy import SQLAlchemy
from flask_mail import Mail

db = SQLAlchemy()
migrate = Migrate()
cache = Cache()
jwt = JWTManager()
mail = Mail()
socketio = SocketIO()
