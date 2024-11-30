# Introduction
Welcome to our project Beacon for CMPT 362. Beacon is a local social network that revolutionizes how we connect with our community through location-based interactions.

### Site
The webpage for our project can be found here: https://beacon-topaz.vercel.app/. 

### Group 13
Zaid Arshad<br>Omar Elshehawi<br>Jonathan Gibbons<br>Lex Rohweder

# Setup
## Server Setup
### Database Setup
To create the database run: `python3 server/database/makeDatabase.py`

### Start Server
To start the server: `python3 server/main.py`

## Android Setup
### Server IP
Set the variable `BACKEND_IP` in `frontend\app\src\main\java\com\example\beacon\utils\Constants.kt` to the IP that the server is hosted on.

```
const val BACKEND_IP = "http://10.0.0.193:3333"
```

### Maps API
In `frontend\local.properties` create the variable `MAPS_API_KEY` and set it to your Google Maps API key. 

```
MAPS_API_KEY=someapikeyhereperhaps
```

To generate a Google Maps key, refer to this page: https://developers.google.com/maps/documentation/android-sdk/get-api-key.