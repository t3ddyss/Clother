from datetime import timedelta
from os.path import join, dirname, realpath

SQLALCHEMY_DATABASE_URI = 'sqlite:///../instance/clother.db'
SQLALCHEMY_TRACK_MODIFICATIONS = False
JWT_ACCESS_TOKEN_EXPIRES = timedelta(hours=1)
JWT_REFRESH_TOKEN_EXPIRES = timedelta(days=30)
JWT_TOKEN_LOCATION = ["headers", "json", "query_string"]
UPLOAD_FOLDER = join(dirname(realpath(__file__)), 'instance/images')
