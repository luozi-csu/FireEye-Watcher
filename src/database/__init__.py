import yaml
import pymysql.cursors

config = yaml.load(open("database/db.yaml"), Loader=yaml.CLoader)

db = pymysql.connect(
    host=config["host"], 
    user=config["user"], 
    password=config["password"], 
    port=config["port"], 
    database=config["name"], 
    cursorclass=pymysql.cursors.DictCursor)