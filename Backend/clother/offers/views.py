import math
from flask import Blueprint, request, jsonify
from .models import Offer
from flask_jwt_extended import jwt_required

blueprint = Blueprint('offers', __name__)

default_page_num = 1
default_page_size = 10


@blueprint.route('/offers')
def get_offers():
    page_num = request.args.get('page', default=default_page_num, type=int)
    page_size = request.args.get('per_page', default=default_page_size, type=int)

    offers = Offer.query.paginate(page=page_num, per_page=page_size)
    total_pages = math.ceil(Offer.query.count() / page_size)

    return jsonify({'total_pages': total_pages, 'results': [offer.to_dict() for offer in offers.items]})
