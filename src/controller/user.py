from log import logger
from flask import Blueprint, request, make_response
from model import User, Token
from service import UserService
from repository import UserRepository
from database import db
from passlib.context import CryptContext
from common import response_success, response_failed
from util import gen_jwt, validate_token

user_repository = UserRepository(conn=db)
user_service = UserService(user_repo=user_repository)
user_controller = Blueprint("user_controller", __name__, url_prefix="/api/v1")
ctx = CryptContext(schemes=["bcrypt"])

def valid(name: str, password: str) -> bool:
    if name == "" or password == "":
        logger.error("empty username or password")
        return False
    elif len(password) < 6:
        logger.error("length of password is shorter than 6")
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
        return make_response(response_success([user.to_json() for user in users], "get all users"), 200)
    
    res = []
    for user in users:
        if user.name == name:
            res.append(user.to_json())
            break

    return make_response(response_success(res, "get user by name"), 200)
    

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
        return make_response(response_failed(400, "invalid username or password"), 400)
    
    users = user_service.list_users()
    for user in users:
        if name == user.name:
            return make_response(response_failed(400, "username already existed"), 400)
    
    password = ctx.hash(password)
    user = User(name=name, password=password)
    user = user_service.create_user(user)

    if user == None:
        return make_response(response_failed(500, "create user failed"), 500)

    return make_response(response_success(
        user.to_json(), 
        "create user successfully"
    ), 200)

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
    name = request.args.get("name", "")
    token = request.headers.get("Authorization")

    payload, _ = validate_token(token)
    if payload["name"] != name:
        logger.error("username not match")
        return make_response(response_failed(400, "username not match"), 400)

    body = request.get_json()
    new_user = body.get("new_user", "{}")

    if not new_user:
        logger.error("empty update user input")
        return make_response(response_failed(400, "empty update user input"), 400)
    
    password = new_user.get("password", "")

    if not valid(name, password):
        return make_response(response_failed(400, "empty update user input"), 400)
    
    user = User(name=name)
    user.password = ctx.hash(password)
    user = user_service.update_user(name, user)

    if user == None:
        return make_response(response_failed(400, "empty update user input"), 500)

    return make_response(response_success(
        user.to_json(),
        "update user successfully"
    ), 200)

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
        logger.error("empty username")
        return make_response(response_failed(400, "empty username"), 400)
    
    user_service.delete_user(name)

    return make_response(response_success(None, "delete user successfully"), 200)

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
        return make_response(response_failed(400, "invalid username or password"), 400)
    
    user = user_service.get_user(name)
    if user == None:
        logger.error("user not found")
        return make_response(response_failed(400, "user not found"), 400)
    
    if not ctx.verify(password, user.password):
        logger.error("wrong password")
        return make_response(response_failed(400, "wrong password"), 400)
    
    token = gen_jwt(user)

    return make_response(response_success(
        data=Token(
            uid=user.id, name=user.name, jwt=token,
            desc="login successfully, please set token in headers"
        ).to_json(),
        desc="login successfully, please set token in headers"
    ), 200)

@user_controller.post("/auth/register")
def register():
    body = request.get_json()
    name = body.get("name", "")
    password = body.get("password", "")
    if not valid(name, password):
        return make_response(response_failed(400, "invalid username or password"), 400)
    
    users = user_service.list_users()
    for user in users:
        if name == user.name:
            return make_response(response_failed(400, "username already existed"), 400)
    
    password = ctx.hash(password)
    user = User(name=name, password=password)
    user = user_service.create_user(user)

    if user == None:
        return make_response(response_failed(500, "create user failed"), 500)

    return make_response(response_success(
        data=user.to_json(),
        desc="register successfully"
    ), 200)

@user_controller.post("/auth/logout")
def logout():
    """
    @Summary logout
    @Description logout
    @Produce json
    @Tags user
    @Success 200 None
    @Router /api/v1/auth/logout [post]
    """
    # stateless logout
    return make_response(response_success(None, "logout successfully"), 200)