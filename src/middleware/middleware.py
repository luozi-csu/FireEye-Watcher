from log import logger

class Middleware(object):
    def __init__(self, old_wsgi_app) -> None:
        self.old_wsgi_app = old_wsgi_app

    def __call__(self, environ: dict, start_response: callable) -> any:
        self.request_logger(environ)
        response = self.old_wsgi_app(environ, start_response)
        return response
    
    def request_logger(self, environ: dict):
        request_method = environ["REQUEST_METHOD"]
        request_uri = environ["REQUEST_URI"]
        remote_addr = environ["REMOTE_ADDR"]
        msg = "[%s] %s, client=%s" % (request_method, request_uri, remote_addr)
        logger.debug(msg)
