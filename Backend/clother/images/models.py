from clother import db


class BaseImage(db.Model):
    __abstract__ = True
    id = db.Column(db.Integer, primary_key=True)
    uri = db.Column(db.String, nullable=False)

    def get_uri(self, url_root):
        if self.__is_local():
            return f'{url_root}api/images/{self.uri}'
        else:
            return self.uri

    def __is_local(self):
        return not self.uri.startswith('https://')
