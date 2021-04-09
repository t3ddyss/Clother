from flask import Blueprint
from ..utils import base_prefix

blueprint = Blueprint('chat', __name__, url_prefix=(base_prefix + '/chat'))

from . import views, events
