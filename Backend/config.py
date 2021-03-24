from datetime import timedelta

SQLALCHEMY_TRACK_MODIFICATIONS = False
JWT_ACCESS_TOKEN_EXPIRES = timedelta(seconds=10)
JWT_REFRESH_TOKEN_EXPIRES = timedelta(days=90)
