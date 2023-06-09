import jwt
import time
from model import User

headers = {
    "alg": "HS256",
    "typ": "JWT"
}

salt = "fireeyewatcher"

EXPIRED_ERROR = 1
DECODE_ERROR = 2
INVALID_TOKEN_ERROR = 3

def gen_jwt(user: User):
    now = int(time.time())
    exp = now + 24 * 3600
    payload = {
        "id": user.id,
        "name": user.name,
        "lastLoginTime": now,
        "exp": exp
    }
    token = jwt.encode(payload=payload, key=salt, algorithm="HS256", headers=headers)
    return token

def validate_token(token: str):
    payload, err = None, None
    try:
        payload = jwt.decode(token, salt, algorithms=["HS256"])
    except jwt.exceptions.ExpiredSignatureError:
        err = EXPIRED_ERROR
    except jwt.exceptions.DecodeError:
        err = DECODE_ERROR
    except jwt.exceptions.InvalidTokenError:
        err = INVALID_TOKEN_ERROR
    return payload, err