from flask.helpers import get_debug_flag
from clother import create_app

config = 'config.py' if get_debug_flag() else 'config.py'
app = create_app(config)

if __name__ == '__main__':
    app.run()
