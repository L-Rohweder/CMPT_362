# Setup Database
To create the database run: `python3 server/database/makeDatabase.py`

# Starting Server
To start the server: `python3 server/main.py`

# Interaction Sending
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
            "longitude": 30.3,
            "imageLink": "http://thisisthehost.com/img",
            "userID": 77,
            "username": "testUsername1"
            }
        //if post has no image, make the link empty quotes ""

# Interaction Receiving
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
                "imageLink": "http://thisisthehost.com/img",
                "userID": 77,
                "username": "testUsername1",
                "datetime": "yyyy-mm-dd hh-mm-ss"
                },
            ]

## Setup

### Google Maps API Key
1. Get a Google Maps API key from the [Google Cloud Console](https://console.cloud.google.com/)
2. Copy `app/src/main/res/values/secrets.xml.template` to `app/src/main/res/values/secrets.xml`
3. Replace `YOUR_MAPS_API_KEY_HERE` with your actual API key in secrets.xml

Note: Never commit secrets.xml to version control!