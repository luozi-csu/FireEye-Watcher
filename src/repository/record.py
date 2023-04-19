from model import Record
from pymysql import Connection

class RecordRepository:
    def __init__(self, conn: Connection) -> None:
        self.conn = conn
        self.cursor = self.conn.cursor()

    def get_record_by_id(self, id: int) -> Record:
        self.ping()
        sql = "SELECT `id`, `uid`, `path`, `request_time`, `finished_time`, `result` FROM `records` WHERE `id`=%s"
        self.cursor.execute(sql, (id,))
        row = self.cursor.fetchone()
        if row == None:
            return None
        return Record(
            id=row["id"], uid=row["uid"], path=row["path"],
            request_time=row["request_time"], finished_time=row["finished_time"],
            result=row["result"]
        )

    def get_records_by_uid(self, uid: int) -> list[Record]:
        self.ping()
        sql = "SELECT `id`, `uid`, `path`, `request_time`, `finished_time`, `result` FROM `records` WHERE `uid`=%s"
        self.cursor.execute(sql, (uid,))
        rows = self.cursor.fetchall()
        records = list()
        for row in rows:
            records.append(
                Record(
                    id=row["id"], uid=row["uid"], path=row["path"],
                    request_time=row["request_time"], finished_time=row["finished_time"],
                    result=row["result"]
                )
            )
        return records

    def create_record(self, record: Record) -> Record:
        self.ping()
        sql = "INSERT INTO `records` (`uid`, `path`, `request_time`, `finished_time`, `result`) VALUES (%s, %s, %s, %s, %s)"
        self.cursor.execute(sql, (record.uid, record.path, record.request_time, record.finished_time, record.result))
        self.conn.commit()
        return record

    def update_record(self, record: Record) -> Record:
        self.ping()
        sql = "UPDATE `records` SET `path`=%s, `request_time`=%s, `finished_time`=%s, `result`=%s WHERE `id`=%s"
        self.cursor.execute(sql, (record.path, record.request_time, record.finished_time, record.result, record.id))
        self.conn.commit()
        return record

    def delete_record(self, record: Record):
        self.ping()
        sql = "DELETE FROM `records` WHERE `id`=%s"
        self.cursor.execute(sql, (record.id))
        self.conn.commit()
        return
    
    def ping(self):
        self.conn.ping(reconnect=True)