from flask import Blueprint
from .models import User
from flask_jwt_extended import jwt_required

from ..utils import base_prefix

blueprint = Blueprint('users', __name__, url_prefix=(base_prefix + '/users'))
