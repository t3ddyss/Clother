from enum import auto

from clother.common.error import MethodError


class ChatError(MethodError):
    IMAGE_LIMIT_EXCEEDED = auto()

