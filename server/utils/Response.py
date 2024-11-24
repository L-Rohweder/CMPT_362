import json

def OK():
    response = "HTTP/1.1 200 OK\r\n"
    response += "Content-Type: text/plain\r\n"
    response += "Content-Length: 2\r\n"  # Length of "OK"
    response += "\r\n"
    response += "OK"
    return response

def OKBODY(body):
    response = "HTTP/1.1 200 OK\r\n"
    response += "Content-Type: application/json\r\n"
    response += f"Content-Length: {len(body)}\r\n"
    response += "\r\n"
    response += body
    return response

def ERROR(message):
    body = json.dumps({"error": message})
    response = "HTTP/1.1 400 Bad Request\r\n"
    response += "Content-Type: application/json\r\n"
    response += f"Content-Length: {len(body)}\r\n"
    response += "\r\n"
    response += body
    return response
