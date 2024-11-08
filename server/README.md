Setup:
    run make database: python3 database/makeDatabase.py 
    or python3 server/database/makeDatabase.py from project root

Starting:
    python3 main.py or python3 server/main.py from root

Interaction Sending:
    Remote Server:
        post to SERVERIP:server_port/post
    Local Machine:
        post to localhost:server_port/post  
    All:
        where server_port is defined in config.ini
        post with body -> 
            {
            "name": "name here",
            "content": "This is the post content",
            "latitude": 30.3,
            "longitude": 30.3
            }

Interaction Receiving:
    Remote Server:
        post to SERVERIP:server_port/get
    Local Machine:
        post to localhost:server_port/get
    All:
        where server_port is defined in config.ini
        post with body -> 
            {
            "latitude": 30.3,
            "longitude": 30.3
            "range": 5
            }
        
        you will receive a body of ->
            [
                {
                "name": "name here",
                "content": "This is the post content",
                "latitude": 30.3,
                "longitude": 30.3,
                "datetime": "yyyy-mm-dd hh-mm-ss"
                },
            ]