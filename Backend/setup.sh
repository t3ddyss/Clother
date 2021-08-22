#!/bin/bash

echo "Setting up a Flask server..."

flask admin create-db
flask admin populate-categories
flask admin mock-users
flask admin mock-offers
flask admin mock-messages

echo "Finished"