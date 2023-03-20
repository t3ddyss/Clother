#!/bin/bash

echo "Setting up a Flask server..."

flask admin create-tables
flask admin populate-categories
flask admin mock-users
flask admin mock-offers
flask admin mock-messages

echo "Setup completed"