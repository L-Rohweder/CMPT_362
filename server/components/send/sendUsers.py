import database.databaseModule as dbModule
import utils.Response as Response
import json
from components.store.storeUser import hash_password  # For password verification

def sendUser(jsonFile, connection, dbConnection):
    try:
        username = jsonFile["username"]
        password = jsonFile["password"]
        
        # Get user
        user = dbModule.get_user_by_username(username, dbConnection)
        if not user:
            connection.sendall(Response.ERROR("Invalid username or password").encode('utf-8'))
            return
            
        # Parse stored password hash and salt
        stored_password = user[8]  # password field
        stored_hash, salt = stored_password.split(":")
        
        # Verify password
        verify_hash, _ = hash_password(password, salt)
        
        if verify_hash == stored_hash:
            response = json.dumps({
                "message": "Login successful",
                "id": user[0],
                "username": user[2],
                "email": user[1],
                "firstname": user[3],
                "lastname": user[4]
            })
            connection.sendall(Response.OKBODY(response).encode('utf-8'))
        else:
            connection.sendall(Response.ERROR("Invalid username or password").encode('utf-8'))
            
    except Exception as e:
        print(f"Login error: {e}")
        connection.sendall(Response.ERROR("Invalid username or password").encode('utf-8'))