def response_success(data: any, desc: str):
    return {
        "status_code": 200,
        "data": data,
        "desc": desc
    }

def response_failed(desc: str):
    return {
        "status_code": 400,
        "data": None,
        "desc": desc
    }