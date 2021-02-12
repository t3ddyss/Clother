from itsdangerous import URLSafeTimedSerializer
from clother import app

ts = URLSafeTimedSerializer(app.config['SECRET_KEY'])
