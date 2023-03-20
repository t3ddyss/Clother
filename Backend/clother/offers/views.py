import json
from http import HTTPStatus

from flask import Blueprint, request, jsonify, abort
from flask_jwt_extended import jwt_required, get_jwt_identity
from sqlalchemy.exc import IntegrityError

from .error import OfferError
from .models import Offer, Category, Location, OfferImage
from .. import db
from clother.common.constants import BASE_PREFIX
from ..common.error import CommonError
from ..images.utils import is_allowed_image, store_images
from ..users.models import User

blueprint = Blueprint('offers', __name__, url_prefix=(BASE_PREFIX + '/offers'))
DEFAULT_OFFERS_PAGE_SIZE = 10


@blueprint.get('')
@jwt_required()
def get_offers():
    after = request.args.get('after', default=None, type=int)
    before = request.args.get('before', default=None, type=int)
    limit = request.args.get('limit', default=DEFAULT_OFFERS_PAGE_SIZE, type=int)
    category = request.args.get('category', default=None, type=int)
    query = request.args.get('query', default=None, type=str)
    size = request.args.get('size', default=None, type=str)
    coordinates = request.args.get('location', default=None, type=str)
    radius = request.args.get('radius', default=None, type=int)
    user = request.args.get('user', default=None, type=int)

    offers_query = Offer.query

    if category:
        offers_query = offers_query.filter(Offer.category_id == category)
    if query:
        offers_query = offers_query.filter(Offer.title.ilike(f'%{query}%'))
    if size:
        offers_query = offers_query.filter(Offer.size.ilike(f'{size}'))
    if coordinates and radius:
        lat, lng = [float(x) for x in coordinates.split(',')]
        offers_query = offers_query.join(Location).filter(Location.distance(lat, lng) <= radius)
    if user:
        offers_query = offers_query.filter(Offer.user_id == user)

    if after is None and before is None:  # initial request
        offers = offers_query.order_by(Offer.id.desc()).limit(limit).all()

    elif before is None:  # append
        offers = offers_query.order_by(Offer.id.desc()).filter(Offer.id < after).limit(limit).all()

    else:  # prepend
        offers = offers_query.order_by(Offer.id.asc()).filter(Offer.id > before).limit(limit).all()
        offers.reverse()

    return jsonify([offer.to_dict(url_root=request.url_root) for offer in offers])


@blueprint.post('/new')
@jwt_required()
def post_offer():
    data = json.loads(request.form['request'])
    user = User.query.get(get_jwt_identity())

    try:
        category_id = data['category_id']
        title = data['title']
        description = data.get('description', None)
        coordinates = data.get('location', None)
        size = data.get('size', None)
    except KeyError:
        return jsonify(CommonError.MISSING_MANDATORY_PARAMETER.to_dict()), HTTPStatus.BAD_REQUEST

    files = request.files.getlist('file')
    if not files:
        return jsonify(OfferError.MISSING_IMAGES.to_dict()), HTTPStatus.BAD_REQUEST
    if len(files) > 5:
        return jsonify(OfferError.IMAGE_LIMIT_EXCEEDED), HTTPStatus.BAD_REQUEST
    if any(not (file and is_allowed_image(file.filename)) for file in files):
        return jsonify(CommonError.UNSUPPORTED_FILE_TYPE.to_dict()), HTTPStatus.BAD_REQUEST

    try:
        offer = Offer(user_id=user.id, category_id=category_id, title=title)
        if description:
            offer.description = description

        if coordinates:
            lat, lng = coordinates.split(',')
            location = Location(latitude=lat, longitude=lng)
            offer.location = location

        if size:
            offer.size = size

        uris = store_images(files)
        for uri in uris:
            offer.images.append(OfferImage(uri=uri))

        db.session.add(offer)
        db.session.commit()

    except (IntegrityError, Exception):
        db.session.rollback()
        return jsonify(CommonError.UNKNOWN_ERROR.to_dict()), HTTPStatus.INTERNAL_SERVER_ERROR

    return {'id': offer.id}


@blueprint.delete('/delete')
@jwt_required()
def delete_offer():
    user = User.query.get(get_jwt_identity())
    offer_id = request.args['offer']
    offer = Offer.query.get(request.args.get('offer', default=None, type=int))

    if offer and offer.user_id == user.id:
        db.session.delete(offer)
        db.session.commit()
        return {'message': 'Offer was successfully deleted'}
    else:
        abort(400)


@blueprint.get('/categories')
@jwt_required()
def get_categories():
    return jsonify([category.to_dict() for category in Category.query.order_by(Category.id.asc())])
