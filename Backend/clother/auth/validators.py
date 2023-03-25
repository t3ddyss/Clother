import re


def validate_name(name):
    return get_name_regex().match(name)


def validate_password(password):
    return get_password_regex().match(password)


def validate_status(status):
    return len(status) <= 70


def get_name_regex():
    return re.compile(r'^(?=[a-zA-Z\s]{2,50}$)')


def get_password_regex():
    return re.compile(r'^(?=\S{8,25}$)(?=.*?\d)(?=.*?[a-z])(?=.*?[A-Z])(?=\S+$)(?=.*?[^A-Za-z\s0-9])')
