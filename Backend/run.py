from clother import create_app, socketio

config = 'config.py'
app = create_app(config)

if __name__ == '__main__':
    socketio.run(app)
