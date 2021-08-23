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
5. Navigate back to `Backend` directory, open command line here and execute `docker-compose up -d --build`. After containers start, visit `http://localhost:5000/` to check that everything is fine. You should get 404 HTTP error
6. Execute `docker-compose exec api bash` to open `bash` inside the Docker container. Then execute `./setup.sh` to create database tables and populate them. If you don't want to populate some tables, just remove the corresponding commands from the `setup.sh`, e.g., `flask admin mock-messages`
7. Now open `Client` folder as Android Studio project
8. Add the following line to the global `gradle.properties` file: `GOOGLE_MAPS_API_KEY="%YOUR_API_KEY%"`. 
9. If you are going to run this app on your physical device, go to `Client/app/src/main/java/com/t3ddyss/clother/utilities/Constants.kt` and change `BASE_URL_DEVICE` to the IP address of your machine
10. Register the app with Firebase as described [here](https://firebase.google.com/docs/cloud-messaging/android/client#register_your_app_with_firebase). You will need to replace `Client/app/google-services.json` with your `google-services.json` file
11. At this point, you should be able to successfully build, run and use the application


## Roadmap

* Implement deep linking for push notifications
* Support images in chat
* ~~Migrate to [PostgreSQL](https://www.postgresql.org/) and add advanced database triggers~~
* ~~Support different timezones~~


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
