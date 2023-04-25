class Token:
    def __init__(
            self,
            uid=0,
            name="",
            jwt="",
            desc="") -> None:
        self.uid = uid
        self.name = name
        self.jwt = jwt
        self.desc = desc

    def to_json(self):
        return {
            "uid": self.uid,
            "name": self.name,
            "jwt": self.jwt,
            "desc": self.desc
        }