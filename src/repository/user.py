from model import User
from pymysql import Connection

class UserRepository:
    def __init__(self, conn: Connection) -> None:
        self.conn = conn
        self.cursor = self.conn.cursor()

    def get_user_by_id(self, id: int) -> User:
        sql = "SELECT `id`, `name`, `password` from `users` WHERE `id`=%s"
        self.cursor.execute(sql, (id,))
        row = self.cursor.fetchone()
        if row == None:
            return None
        return User(id=row["id"], name=row["name"], password=row["password"])
    
    def get_user_by_name(self, name: str) -> User:
        sql = "SELECT `id`, `name`, `password` from `users` WHERE `name`=%s"
        self.cursor.execute(sql, (name,))
        row = self.cursor.fetchone()
        if row == None:
            return None
        return User(id=row["id"], name=row["name"], password=row["password"])
    
    def list_users(self) -> list[User]:
        sql = "SELECT `id`, `name`, `password` from `users`"
        self.cursor.execute(sql)
        rows = self.cursor.fetchall()
        users = list()
        for row in rows:
            users.append(User(id=row["id"], name=row["name"], password=row["password"]))
        return users
    
    def create_user(self, user: User) -> User:
        sql = "INSERT INTO `users` (`name`, `password`) VALUES (%s, %s)"
        self.cursor.execute(sql, (user.name, user.password))
        self.conn.commit()
        return user
    
    def update_user(self, user: User) -> User:
        sql = "UPDATE `users` SET `name`=%s, `password`=%s WHERE `id`=%s"
        self.cursor.execute(sql, (user.name, user.password, user.id))
        self.conn.commit()
        return user
    
    def delete_user(self, user: User):
        sql = "DELETE FROM `users` WHERE `name`=%s"
        self.cursor.execute(sql, (user.name,))
        self.conn.commit()
        return
    