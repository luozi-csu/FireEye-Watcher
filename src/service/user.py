import logging
from model import User
from repository import UserRepository

class UserService:
    def __init__(self, user_repo: UserRepository) -> None:
        self.user_repo = user_repo

    def get_user(self, name: str) -> User:
        if name == "":
            logging.error("empty user name")
            return None
        return self.user_repo.get_user_by_name(name=name)
    
    def list_users(self) -> list[User]:
        return self.user_repo.list_users()
    
    def create_user(self, user: User) -> User:
        if user.name == "" or user.password == "":
            logging.error("empty username or password")
            return None
        return self.user_repo.create_user(user=user)
    
    def update_user(self, name: str, user: User) -> User:
        old_user = self.user_repo.get_user_by_name(name=name)
        if old_user == None:
            logging.error("user does not exist")
            return None
        if old_user.id != user.id:
            user.id = old_user.id
        if user.name == "":
            user.name = old_user.name
        return self.user_repo.update_user(user=user)
    
    def delete_user(self, name: str):
        if name == "":
            logging.error("empty username")
            return
        user = User(name=name)
        self.user_repo.delete_user(user=user)
        