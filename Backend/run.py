from clother import create_app, socketio

config = 'config.py'

if __name__ == '__main__':
    app = create_app(config)
    socketio.run(app)
else:
    gunicorn_app = create_app(config)
