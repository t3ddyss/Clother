import os
from datetime import timedelta
from os.path import join, dirname, realpath

SQLALCHEMY_TRACK_MODIFICATIONS = False
SQLALCHEMY_DATABASE_URI = os.getenv('DATABASE_URL')
JWT_ACCESS_TOKEN_EXPIRES = timedelta(hours=1)
JWT_REFRESH_TOKEN_EXPIRES = timedelta(days=30)
UPLOAD_FOLDER = join(dirname(realpath(__file__)), 'instance/images')
