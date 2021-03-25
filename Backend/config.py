from datetime import timedelta

SQLALCHEMY_TRACK_MODIFICATIONS = False
JWT_ACCESS_TOKEN_EXPIRES = timedelta(minutes=15)
JWT_REFRESH_TOKEN_EXPIRES = timedelta(days=30)
