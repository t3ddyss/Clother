import time

from flask import Blueprint, request, jsonify
from .models import Offer
from flask_jwt_extended import jwt_required

from ..utils import response_delay, base_prefix

blueprint = Blueprint('offers', __name__, url_prefix=(base_prefix + '/offers'))

default_page_size = 10


@blueprint.route('')
@jwt_required()
def get_offers():
    after = request.args.get('after', default=None, type=int)
    before = request.args.get('before', default=None, type=int)
    limit = request.args.get('size', default=default_page_size, type=int)

    if after is None and before is None:  # initial request
        offers = Offer.query.order_by(Offer.id.desc()).limit(limit).all()

    elif before is None:  # append
        offers = Offer.query.order_by(Offer.id.desc()).filter(Offer.id < after).limit(limit).all()

    else:  # prepend
        offers = Offer.query.order_by(Offer.id.asc()).filter(Offer.id > before).limit(limit).all()
        offers.reverse()

    return jsonify([offer.to_dict() for offer in offers])


# Simulate response delay while testing app on localhost
@blueprint.before_request
def simulate_delay():
    time.sleep(response_delay)
