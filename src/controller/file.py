import datetime
from flask import Blueprint, request
from common import response_success, response_failed

file_controller = Blueprint("file_controller", __name__, url_prefix="/api/v1")

@file_controller.post("/upload")
def upload_file():
    f = request.files['upload']
    f.save("tmp/" + f.filename)
    return response_success(None, "upload file successfully")