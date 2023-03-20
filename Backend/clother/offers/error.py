from enum import auto

from clother.common.error import MethodError


class OfferError(MethodError):
    MISSING_IMAGES = auto()
    IMAGE_LIMIT_EXCEEDED = auto()
