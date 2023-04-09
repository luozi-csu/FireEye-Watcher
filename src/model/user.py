class User:
    def __init__(
            self,
            id=0,
            name="",
            password="") -> None:
        self.id = id
        self.name = name
        self.password = password

    def to_json(self):
        return {
            "id": self.id,
            "name": self.name
        }