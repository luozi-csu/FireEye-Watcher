from log import logger
from collections import OrderedDict
from flask import Blueprint, request, make_response
from model import Statistics, DailyFreq
from service import RecordService
from repository import RecordRepository
from database import db
from common import response_success, response_failed
from util import validate_token, convert_to_date

record_repository = RecordRepository(conn=db)
record_service = RecordService(record_repo=record_repository)
statistics_controller = Blueprint("statistics_controller", __name__, url_prefix="/api/v1")

OVERHEAT_STATUS = 2
NORMAL_STATUS = 1
UNDERHEAT_STATUS = 0
PROCESSING_STATUS = -1

@statistics_controller.get("/statistics")
def get_statistics():
    uid = int(request.args.get("uid", 0))
    token = request.headers.get("Authorization")

    if uid == 0:
        return make_response(response_failed(400, "need uid"), 400)
    
    payload, _ = validate_token(token)
    if payload["id"] != uid:
        logger.error("user not match")
        return make_response(response_failed(400, "user not match"), 400)
    
    records = record_service.get_records_by_uid(uid)

    if len(records) == 0:
        logger.info("user=%d has no record" % uid)
        return make_response(response_success(None, "user=%d has no record" % uid))
    
    overheat_num, normal_num, underheat_num, processing_num = 0, 0, 0, 0
    daily_dict = {}
    
    for record in records:
        if record.result >= OVERHEAT_STATUS:
            overheat_num += 1
        elif record.result >= NORMAL_STATUS:
            normal_num += 1
        elif record.result >= UNDERHEAT_STATUS:
            underheat_num += 1
        elif record.result >= PROCESSING_STATUS:
            processing_num += 1

        date = convert_to_date(record.request_time)
        if date in daily_dict:
            daily_dict[date] += 1
        else:
            daily_dict.setdefault(date, 1)

    ordered_daily_dict = OrderedDict(sorted(daily_dict.items(), key=lambda x: x[0]))
    ordered_freqs : list[DailyFreq] = []
    for key, value in ordered_daily_dict.items():
        daily_freq = DailyFreq(key, value)
        ordered_freqs.append(daily_freq)

    daily_freqs : list[DailyFreq] = [ordered_freqs[0]]
    for i in range(1, len(ordered_freqs)):
        for date in range(daily_freqs[-1].date+86400, ordered_freqs[i].date, 86400):
            daily_freqs.append(DailyFreq(date, 0))
        daily_freqs.append(ordered_freqs[i])

    statistics = Statistics(uid, overheat_num, normal_num, underheat_num, processing_num,
                            daily_freqs)
    
    return make_response(response_success(statistics.to_json(), "get statistics info successfully"))