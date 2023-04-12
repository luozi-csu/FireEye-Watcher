import logging
from flask import Blueprint, request, session
from model import User, Token
from service import UserService
from repository import UserRepository
from database import db
from passlib.context import CryptContext
from common import response_success, response_failed
from util import gen_jwt

user_repository = UserRepository(conn=db)
user_service = UserService(user_repo=user_repository)
user_controller = Blueprint("user_controller", __name__, url_prefix="/api/v1")
ctx = CryptContext(schemes=["bcrypt"])

def valid(name: str, password: str) -> bool:
    if name == "" or password == "":
        logging.error("empty username or password")
        return False
    elif len(password) < 6:
        logging.error("length of password is shorter than 6")
        return False
    return True

@user_controller.get("/users")
def get_users():
    """
    @Summary get users
    @Description get users
    @Produce json
    @Tags user
    @Param name param str false "username"
    @Success 200 model.User[]
    @Router /api/v1/users [get]
    """
    name = request.args.get("name", "")
    users = user_service.list_users()

    if name == "":
        return response_success([user.to_json() for user in users], "get all users")
    
    res = []
    for user in users:
        if user.name == name:
            res.append(user.to_json())
            break

    return response_success(res, "get user by name")
    

@user_controller.post("/users")
def create_users():
    """
    @Summary create user
    @Description create user
    @Accept json
    @Produce json
    @Tags user
    @Param user body model.User true "user info"
    @Success 200 model.User
    @Router /api/v1/users [post]
    """
    body = request.get_json()
    name = body.get("name", "")
    password = body.get("password", "")
    if not valid(name, password):
        return response_failed("invalid username or password")
    
    password = ctx.hash(password)
    user = User(name=name, password=password)

    return response_success(
        user_service.create_user(user).to_json(), 
        "create user successfully"
    )

@user_controller.put("/users")
def update_user():
    """
    @Summary update user
    @Description update user
    @Accept json
    @Produce json
    @Tags user
    @Param user body model.User true "user info"
    @Success 200 model.User
    @Router /api/v1/users [put]
    """
    body = request.get_json()
    name = body.get("name", "")
    password = body.get("password", "")
    if not valid(name, password):
        return response_failed("invalid username or password")
    
    user = user_service.get_user(name)
    if user == None:
        logging.error("user not found")
        return response_failed("user not found")
    
    user.password = ctx.hash(user.password)

    return response_success(
        user_service.update_user(name, user).to_json(),
        "update user successfully"
    )

@user_controller.delete("/users")
def delete_user():
    """
    @Summary delete user
    @Description delete user
    @Produce json
    @Tags user
    @Param name param str true "username"
    @Success 200 None
    @Router /api/v1/users [delete]
    """
    name = request.args.get("name", "")
    if name == "":
        logging.error("empty username")
        return response_failed("empty username")
    
    user_service.delete_user(name)

    return response_success(None, "delete user successfully")

@user_controller.post("/auth/login")
def login():
    """
    @Summary login
    @Description login
    @Accept json
    @Produce json
    @Tags user
    @Param user body model.User true "user info"
    @Success 200 jwt_token
    @Router /api/v1/auth/login [post]
    """
    body = request.get_json()
    name = body.get("name", "")
    password = body.get("password", "")
    if not valid(name, password):
        return response_failed("invalid username or password")
    
    user = user_service.get_user(name)
    if user == None:
        logging.error("user not found")
        return response_failed("user not found")
    
    if not ctx.verify(password, user.password):
        logging.error("wrong password")
        return response_failed("wrong password")
    
    token = gen_jwt(user)
    session[user.name] = token
    
    return response_success(
        data=Token(jwt=token, desc="login successfully, please set token in headers").to_json(),
        desc="login successfully, please set token in headers"
    )

@user_controller.post("/auth/logout")
def logout():
    """
    @Summary logout
    @Description logout
    @Produce json
    @Tags user
    @Param name param str true "username"
    @Success 200 None
    @Router /api/v1/auth/logout [post]
    """
    name = request.args.get("name", "")
    session.pop(name, None)
    
    return response_success(None, "logout successfully")