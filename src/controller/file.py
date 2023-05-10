import os
import time
import shutil
from log import logger
from flask import Blueprint, request, send_file, make_response
from common import response_success, response_failed
from model import Record
from service import RecordService
from repository import RecordRepository
from database import db
from algorithm.yolov5 import detect, classify
from multiprocessing import Process
from util import *
from pathlib import Path

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
    # move processed video
    video_path = "algorithm/yolov5/runs/detect/exp" + "/" + path.split("/")[-1]
    shutil.move(video_path, "/var/upload/fireeye/processed/{id}.mp4".format(id=record.id))
    logger.debug("update record successfully")

@file_controller.post("/upload")
def upload_file():
    f = request.files['upload']
    path = "/var/upload/fireeye/origin/" + f.filename
    f.save(path)

    token = request.headers.get("Authorization")
    # already validated through middleware
    payload, _ = validate_token(token)

    record = Record(uid=payload["id"], path=path, request_time=int(time.time()))
    record_service.create_record(record)
    
    p = Process(target=process_video, args=(payload["id"],path,))
    p.start()

    return make_response(response_success(record.to_json(), "upload video successfully"), 200)

@file_controller.get("/videos/<int:id>")
def get_video(id):
    record = record_service.get_record(id)
    if record == None:
        return make_response(response_failed(400, "record not found"), 400)
    
    video_path = "/var/upload/fireeye/processed" + "/" + "{id}.mp4".format(id=id)
    if not os.path.exists(video_path):
        return make_response(response_failed(400, "video file not found"), 400)
    
    return make_response(send_file(video_path), 200)