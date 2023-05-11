import time
from datetime import datetime

def convert_to_date(t:int):
    time_array = time.localtime(t)
    ntctime_str = time.strftime("%Y-%m-%d", time_array)
    ntctime_dt = datetime.strptime(ntctime_str, "%Y-%m-%d")
    time_array = time.strptime(str(ntctime_dt), "%Y-%m-%d %H:%M:%S")
    timestamp = int(time.mktime(time_array))
    return timestamp