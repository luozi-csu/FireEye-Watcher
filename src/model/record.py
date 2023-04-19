class Record:
    def __init__(
            self,
            id=0,
            uid=0,
            path="",
            request_time=0,
            finished_time=0,
            result=-1) -> None:
        self.id = id
        self.uid = uid
        self.path = path
        self.request_time = request_time
        self.finished_time = finished_time
        self.result = result

    def to_json(self):
        return {
            "id": self.id,
            "uid": self.uid,
            "path": self.path,
            "request_time": self.request_time,
            "finished_time": self.finished_time,
            "result": self.result
        }