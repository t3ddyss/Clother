# syntax=docker/dockerfile:1

FROM python:3.9.5-slim-buster

WORKDIR /usr/src/app
COPY . /usr/src/app
RUN mkdir -p ./instance/images

RUN pip install --upgrade pip
RUN pip install -r requirements.txt
RUN pip install "Flask[async]"

ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONUNBUFFERED 1

RUN apt-get update && apt-get install -y \
    curl \
    netcat \
    && apt-get -y autoclean

EXPOSE 5000

RUN chmod +x ./entrypoint.sh
ENTRYPOINT ["./entrypoint.sh"]