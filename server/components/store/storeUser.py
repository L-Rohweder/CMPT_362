import database.databaseModule as dbModule
import hashlib
import secrets

def hash_password(password, salt=None):
    if salt is None:
        salt = secrets.token_hex(16)
    hash_obj = hashlib.pbkdf2_hmac('sha256', password.encode(), salt.encode(), 100000)
    return hash_obj.hex(), salt

def storeUser(jsonFile, dbConnection):
    try:
        username = jsonFile["username"]
        password = jsonFile["password"]
        email = jsonFile.get("email", "")
        firstname = jsonFile.get("firstname", "")
        lastname = jsonFile.get("lastname", "")
        
        # Check if username exists
        if dbModule.get_user_by_username(username, dbConnection):
            return False
            
        # Hash password
        password_hash, salt = hash_password(password)
        
        # Store user
        user_id = dbModule.save_user(
            email=email,
            username=username,
            firstname=firstname,
            lastname=lastname,
            password=f"{password_hash}:{salt}",  # Store hash and salt together
            dbConnection=dbConnection
        )
        
        return user_id is not None
            
    except Exception as e:
        print(f"Registration error: {e}")
        return False