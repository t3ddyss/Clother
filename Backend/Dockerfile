# syntax=docker/dockerfile:1

FROM python:3.8-slim-buster

COPY . /app
WORKDIR /app
RUN mkdir ./instance/images

RUN pip3 install -r requirements.txt

ENV FLASK_ENV=development
ENV FLASK_DEBUG=0
ENV FLASK_APP=run.py

RUN ["flask", "admin", "create_db"]
RUN ["flask", "admin", "populate_categories"]
RUN ["flask", "admin", "mock_users"]
RUN ["flask", "admin", "mock_messages"]
RUN ["flask", "admin", "mock_offers"]

CMD ["gunicorn", "--bind", "0.0.0.0:5000", "--worker-class", "eventlet", "-w", "1", "run:gunicorn_app"]