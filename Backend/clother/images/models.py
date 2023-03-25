from flask import request

from clother import db


class BaseImage(db.Model):
    __abstract__ = True
    id = db.Column(db.Integer, primary_key=True)
    uri = db.Column(db.String, nullable=False)

    def get_url(self):
        if self.is_local():
            return f'{request.url_root}api/images/{self.uri}'
        else:
            return self.uri

    def is_local(self):
        return not self.uri.startswith('https://')
