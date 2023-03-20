from flask import Blueprint
from clother.common.constants import BASE_PREFIX

blueprint = Blueprint('chat', __name__, url_prefix=(BASE_PREFIX + '/chat'))

from . import views, events
