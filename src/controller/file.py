import time
from log import logger
from flask import Blueprint, request
from common import response_success
from model import Record
from service import RecordService
from repository import RecordRepository
from database import db
from algorithm.yolov5 import detect, classify
from multiprocessing import Process
from util import *

record_repository = RecordRepository(conn=db)
record_service = RecordService(record_repo=record_repository)
file_controller = Blueprint("file_controller", __name__, url_prefix="/api/v1")

def process_video(uid, path):
    opt = detect.parse_opt(path)
    detect.main(opt)
    res = classify.model_predict(path)
    records = record_service.get_records_by_uid(uid)
    record = None
    for old_record in records:
        if old_record.path == path:
            record = old_record
            break

    if record == None:
        logger.error("record not found: uid=%d, path=%s" % uid, path)
        return
    
    record.result = res
    record.finished_time = int(time.time())
    record_service.update_record(record.id, uid, record)
    logger.debug("update record successfully")

@file_controller.post("/upload")
def upload_file():
    f = request.files['upload']
    path = "/var/upload/fireeye/" + f.filename
    f.save(path)

    token = request.headers.get("Authorization")
    # already validated through middleware
    payload, _ = validate_token(token)

    record = Record(uid=payload["id"], path=path, request_time=int(time.time()))
    record_service.create_record(record)
    
    p = Process(target=process_video, args=(payload["id"],path,))
    p.start()

    return response_success(record.to_json(), "upload video successfully")