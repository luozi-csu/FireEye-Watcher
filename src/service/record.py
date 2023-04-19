from log import logger
from model import Record
from repository import RecordRepository

class RecordService:
    def __init__(self, record_repo: RecordRepository) -> None:
        self.record_repo = record_repo

    def get_record(self, id: int) -> Record:
        return self.record_repo.get_record_by_id(id)

    def get_records_by_uid(self, uid: int) -> list[Record]:
        return self.record_repo.get_records_by_uid(uid)

    def create_record(self, record: Record) -> Record:
        if record == None:
            return None
        if record.uid <= 0:
            logger.error("invalid user id")
            return None
        return self.record_repo.create_record(record)

    def update_record(self, id: int, uid: int, record: Record) -> Record:
        old_record = self.record_repo.get_record_by_id(id)
        if old_record == None:
            logger.error("record %d not found" % id)
            return None
        if old_record.uid != record.uid:
            logger.error("user id not match")
            return None
        if old_record.id != record.id:
            record.id = old_record.id
        return self.record_repo.update_record(record)

    def delete_record(self, id: int):
        record = Record(id=id)
        self.record_repo.delete_record(record)