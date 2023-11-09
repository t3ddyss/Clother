# syntax=docker/dockerfile:1

FROM python:3.11.2-slim-buster

WORKDIR /usr/src/app
COPY .. /usr/src/app
RUN mkdir -p ./media

RUN apt-get update  \
    && apt-get install -y curl netcat libpq-dev gcc \
    && apt-get -y autoclean \
RUN pip install --upgrade pip
RUN pip install -r requirements.txt

ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONUNBUFFERED 1

EXPOSE 5000

RUN chmod +x ./docker/entrypoint.sh
ENTRYPOINT ["./docker/entrypoint.sh"]