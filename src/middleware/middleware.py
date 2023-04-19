import yaml
from log import logger
from flask import Flask, request, make_response
from flask.ctx import RequestContext
from common import response_failed
from util import *

authorization = "Authorization"
whitelist = yaml.load(open("middleware/whitelist.yaml"), Loader=yaml.CLoader)["whitelist"]

class Middleware(object):
    def __init__(self, old_wsgi_app) -> None:
        self.ctx: RequestContext = None
        self.old_wsgi_app = old_wsgi_app

    def __call__(self, environ: dict, start_response: callable) -> any:
        self.ctx = RequestContext(Flask(__name__), environ)
        self.ctx.push()

        self.request_logger(environ)
        msg = self.authenticate(environ)
        if not (msg is None):
            response = make_response(response_failed(401, msg), 401)
            app_iter = response(environ, start_response)
            self.ctx.pop()
            return app_iter
        
        response = self.old_wsgi_app(environ, start_response)
        self.ctx.pop()
        return response
    
    def request_logger(self, environ: dict):
        request_method = environ["REQUEST_METHOD"]
        request_uri = environ["REQUEST_URI"]
        remote_addr = environ["REMOTE_ADDR"]
        msg = "[%s] %s, client=%s" % (request_method, request_uri, remote_addr)
        logger.debug(msg)

    def authenticate(self, environ: dict):
        path_info = environ["PATH_INFO"]
        if path_info in whitelist:
            return None

        token = request.headers.get(authorization, "")
        if token == "":
            return "authorization info not found"
        
        payload, err = validate_token(token)
        if err == EXPIRED_ERROR:
            return "login is expired, please login again"
        elif err == DECODE_ERROR:
            return "decode authorization info error"
        elif err == INVALID_TOKEN_ERROR:
            return "invalid authorization info"
        
        logger.debug("login user: %s" % payload)
        return None