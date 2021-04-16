from flask import Blueprint

from ..utils import base_prefix

blueprint = Blueprint('users', __name__, url_prefix=(base_prefix + '/users'))
