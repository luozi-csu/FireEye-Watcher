import jwt
import time
from model import User

headers = {
    "alg": "HS256",
    "typ": "JWT"
}

salt = "fireeyewatcher"
exp = int(time.time() + 24 * 3600)

def gen_jwt(user: User):
    payload = {
        "name": user.name,
        "exp": exp
    }
    token = jwt.encode(payload=payload, key=salt, algorithm="HS256", headers=headers)
    return token
