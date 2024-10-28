def OK():
    response = "HTTP/1.1 200 OK\r\n"
    response += "Content-Type: text/plain\r\n"
    response += "Content-Length: 2\r\n"  # Length of "OK"
    response += "\r\n"
    response += "OK"
    return response
