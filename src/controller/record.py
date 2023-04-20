from log import logger
from flask import Blueprint, request
from service import RecordService
from repository import RecordRepository
from database import db
from common import response_success, response_failed

record_repository = RecordRepository(conn=db)
record_service = RecordService(record_repo=record_repository)
record_controller = Blueprint("record_controller", __name__, url_prefix="/api/v1")

@record_controller.get("/records")
def get_records_by_uid():
    uid = request.args.get("uid", 0)
    if uid == 0:
        return response_failed(400, "need uid")
    
    records = record_service.get_records_by_uid(uid)
    res = []
    for record in records:
        res.append(record.to_json())

    return response_success(res, "get records by uid")

@record_controller.get("/records/<int:id>")
def get_record(id):
    record = record_service.get_record(id)
    if record == None:
        return response_success(None, "record not found")
    return response_success(record.to_json(), "get record by id")

@record_controller.delete("/records/<int:id>")
def delete_record(id):
    record_service.delete_record(id)
    return response_success(None, "delete record successfully")