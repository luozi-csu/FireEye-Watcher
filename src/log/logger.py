import logging
import yaml

config = yaml.load(open("log/log.yaml"), Loader=yaml.CLoader)

logger = logging.getLogger(config["name"])
logger.setLevel(config["level"])
handler = logging.FileHandler(filename=config["file"], encoding="utf-8")
formatter = logging.Formatter(
    "%(asctime)s - %(levelname)s - %(filename)s - %(funcName)s - %(lineno)s - %(message)s"
)
handler.setFormatter(formatter)
logger.addHandler(handler)