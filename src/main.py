import yaml
from flask import Flask
from controller import user_controller, file_controller

config = yaml.load(open("config.yaml"), Loader=yaml.CLoader)

app = Flask(__name__)
app.secret_key = config["secret"]
app.register_blueprint(user_controller)
app.register_blueprint(file_controller)

if __name__ == "__main__":
    app.run(port=config["port"])