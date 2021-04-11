from flask.helpers import get_debug_flag
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
socketio = None

if get_debug_flag():
    socketio = SocketIO(logger=True)
else:
    socketio = SocketIO(logger=True, async_mode='eventlet')
