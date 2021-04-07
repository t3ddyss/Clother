import json
import os
import secrets
import time

from flask import Blueprint, request, jsonify, current_app
from flask_jwt_extended import jwt_required, get_jwt_identity
from werkzeug.utils import secure_filename

from .models import Offer, Category, Location, Image
from .. import db
from ..utils import response_delay, base_prefix, allowed_file

blueprint = Blueprint('offers', __name__, url_prefix=(base_prefix + '/offers'))

default_page_size = 10


@blueprint.route('')
@jwt_required()
def get_offers():
    after = request.args.get('after', default=None, type=int)
    before = request.args.get('before', default=None, type=int)
    limit = request.args.get('size', default=default_page_size, type=int)
    category = request.args.get('category', default=None, type=int)
    query = request.args.get('query', default=None, type=str)

    offers_query = Offer.query

    if category:
        offers_query = offers_query.filter(Offer.category_id == category)
    if query:
        query = f'%{query}%'
        offers_query = offers_query.filter(Offer.title.ilike(query))

    if after is None and before is None:  # initial request
        offers = offers_query.order_by(Offer.id.desc()).limit(limit).all()

    elif before is None:  # append
        offers = offers_query.order_by(Offer.id.desc()).filter(Offer.id < after).limit(limit).all()

    else:  # prepend
        offers = offers_query.order_by(Offer.id.asc()).filter(Offer.id > before).limit(limit).all()
        offers.reverse()

    return jsonify([offer.to_dict() for offer in offers])


@blueprint.route('/new', methods=['POST'])
@jwt_required()
def post_offer():
    data = json.loads(request.form['request'])

    user_id = get_jwt_identity()
    category_id = data.get('category_id', None)
    title = data.get('title', None)
    description = data.get('description', None)
    coordinates = data.get('location', None)
    size = data.get('size', None)

    if not title or not category_id:
        return {"message": "Please specify title and category"}, 400

    files = request.files.getlist('file')
    if not files:
        return {"message": "Missing images in request"}, 400
    if len(files) > 10:
        return {"message": "You cannot upload more than 10 images"}, 400
    for file in files:
        if not (file and allowed_file(file.filename)):
            return {"message": "This file type is not allowed"}, 400

    try:
        offer = Offer(user_id=user_id, category_id=category_id, title=title)
        if description:
            offer.description = description

        if coordinates:
            lat, lng = coordinates.split(',')
            location = Location(latitude=lat, longitude=lng)
            offer.location = location

        if size:
            offer.size = size

        for file in files:
            filename = secrets.token_urlsafe(10) + secure_filename(file.filename)
            image = Image(uri="api/images/" + filename)

            file.save(os.path.join(current_app.config['UPLOAD_FOLDER'], filename))
            offer.images.append(image)

        db.session.add(offer)
        db.session.commit()

    except Exception as ex:
        db.session.rollback()
        return {"message": "Unknown error"}, 400

    return {'message': 'Successfully created a new offer'}


@blueprint.route('/categories')
@jwt_required()
def get_categories():
    return jsonify([category.to_dict() for category in Category.query.order_by(Category.id.asc())])


# Simulate response delay while testing app on localhost
@blueprint.before_request
def simulate_delay():
    time.sleep(response_delay)
