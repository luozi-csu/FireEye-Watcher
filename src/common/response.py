def response_success(data: any, desc: str):
    return {
        "status_code": 200,
        "data": data,
        "desc": desc
    }

def response_failed(status_code: int, desc: str):
    return {
        "status_code": status_code,
        "data": None,
        "desc": desc
    }