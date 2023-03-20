from enum import auto

from clother.common.error import MethodError


class AuthError(MethodError):
    INVALID_NAME = auto()
    INVALID_EMAIL = auto()
    INVALID_PASSWORD = auto()
    INVALID_CREDENTIALS = auto()
    INVALID_TOKEN = auto()
    TOKEN_EXPIRED = auto()
    EMAIL_OCCUPIED = auto()
    EMAIL_NOT_VERIFIED = auto()
    USER_NOT_FOUND = auto()
