class Token:
    def __init__(
            self,
            jwt="",
            desc="") -> None:
        self.jwt = jwt
        self.desc = desc

    def to_json(self):
        return {
            "jwt": self.jwt,
            "desc": self.desc
        }