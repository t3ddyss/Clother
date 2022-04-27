from flask import Blueprint

from ..constants import BASE_PREFIX

blueprint = Blueprint('users', __name__, url_prefix=(BASE_PREFIX + '/users'))
