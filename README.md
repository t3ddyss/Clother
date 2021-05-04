## Overview

Clother is an Android client-server application for swapping unused clothes written in Python and Kotlin.


### Demonstration
https://user-images.githubusercontent.com/36851587/117014198-3de23080-acf9-11eb-9c3e-7660fcaf0d35.mov

### Features

* Authentication
  * Registration with email confirmation
  * Login 
  * Password recovery
* Paginated list of offers
* Search by text query or category with the following filters: size, location
* Creation of your own offer with up to 5 images, category, title, description, size and location
* Realtime person-to-person chat with push notifications support
* Offers and messages caching

### Built with
* [Flask](https://github.com/pallets/flask)
* [Kotlin coroutines](https://github.com/Kotlin/kotlinx.coroutines)


## Getting started

Follow the steps below to run the application.

### Prerequisites
1. Install [Docker](https://www.docker.com/get-started)
2. Obtain Google Maps API key as described [here](https://developers.google.com/maps/gmp-get-started#create-project)
3. Create Firebase Cloud Messaging project as described [here](https://firebase.google.com/docs/cloud-messaging/android/client) and obtain API key

### Installation

1. Clone the repository and navigate to `Backend` directory
2. Create `instance` folder and `config.py` file inside this folder. This folder should be added to `.gitignore` because it will contain your sensitive data
3. Open `config.py` file and specify the following parameters: `SECRET_KEY`, `JWT_SECRET_KEY`, `MAIL_SERVER`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`,
`MAIL_DEFAULT_SENDER`, `MAIL_USE_TLS`, `MAIL_USE_SSL`, `FCM_API_KEY` (your Firebase Cloud Messaging API key)
4. Open Docker Desktop and wait for the engine to start
5. Navigate back to `Backend` directory, open command line there and execute `docker build -t clother:latest .`. After some time (usually less than 2 minutes) docker image will be created. If you don't want to mock users, messages or offers just remove corresponding lines of code from `Dockerfile` (e.g. `RUN ["flask", "admin", "mock_offers"]`)
6. Execute `docker run -p 5000:5000 clother:latest`. To check that everything is fine, visit `http://localhost:5000/`. You should get 404 HTTP error
7. Now open `Client` folder as Android Studio project
8. Add the following line to `gradle.properties` (global) file: `GOOGLE_MAPS_API_KEY="%YOUR_API_KEY%"`. 
9. If you are going to run this app on your physical device, go to `Client/app/src/main/java/com/t3ddyss/clother/utilities/Constants.kt` and change `BASE_URL_DEVICE` to IP address of your machine
10. Register app with Firebase as described [here](https://firebase.google.com/docs/cloud-messaging/android/client#register_your_app_with_firebase). You will need to replace `Client/app/google-services.json` with your own `google-services.json` file
11. At this point, you should be able to successfully build, run and use application


## Roadmap

* Migrate to [PostgreSQL](https://www.postgresql.org/) and add advanced triggers to database
* Support Room database migrations
* Implement deep linking for push notifications
* Support different timezones
* Support images in chat


## License

Distributed under the MIT License. See `LICENSE` for more information.

## Contact

Telegram: [@t3ddys](https://t.me/t3ddys)    
Github repository: [https://github.com/t3ddyss/Clother](https://github.com/t3ddyss/Clother)

## Acknowledgements

* [eventlet](https://github.com/eventlet/eventlet)
* [Flask-JWT-Extended](https://github.com/vimalloc/flask-jwt-extended)
* [Flask-SocketIO](https://github.com/miguelgrinberg/Flask-SocketIO)
* [Flask-SQLAlchemy](https://github.com/pallets/flask-sqlalchemy)
* [itsdangerous](https://github.com/pallets/itsdangerous)
* [PyFCM](https://github.com/olucurious/PyFCM)
* [Compressor](https://github.com/zetbaitsu/Compressor)
* [Glide](https://github.com/bumptech/glide)
* [Hilt](https://github.com/google/dagger/tree/master/java/dagger/hilt)
* [OkHttp](https://github.com/square/okhttp)
* [Paging Library 3](https://developer.android.com/topic/libraries/architecture/paging/v3-overview)
* [Retrofit 2](https://github.com/square/retrofit)
* [Room](https://developer.android.com/training/data-storage/room)
