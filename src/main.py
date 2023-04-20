import yaml
from flask import Flask
from middleware import Middleware
from controller import user_controller, file_controller, record_controller

config = yaml.load(open("config.yaml"), Loader=yaml.CLoader)

app = Flask(__name__)
app.secret_key = config["secret"]
app.wsgi_app = Middleware(app.wsgi_app)
app.register_blueprint(user_controller)
app.register_blueprint(file_controller)
app.register_blueprint(record_controller)

if __name__ == "__main__":
    app.run(host=config["host"], port=config["port"])