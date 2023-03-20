from enum import StrEnum, auto


class MethodError(StrEnum):
    def to_dict(self):
        return {'error': self.value}


class CommonError(MethodError):
    UNKNOWN_ERROR = auto()
    UNSUPPORTED_MIME_TYPE = auto()
    UNSUPPORTED_FILE_TYPE = auto()
    MISSING_MANDATORY_PARAMETER = auto()
