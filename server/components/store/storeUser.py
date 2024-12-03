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
        print("Starting user registration process...")
        username = jsonFile["username"]
        password = jsonFile["password"]
        email = jsonFile.get("email", "")
        firstname = jsonFile.get("firstname", "")
        lastname = jsonFile.get("lastname", "")
        
        # First check if username exists
        existing_user = dbModule.get_user_by_username(username, dbConnection)
        if existing_user:
            print(f"Username {username} already exists")
            return False
            
        # If username doesn't exist, proceed with registration
        print(f"Username {username} is available, proceeding with registration")
        
        # Hash password
        print("Hashing password...")
        password_hash, salt = hash_password(password)
        
        # Store user
        print("Attempting to save user to database...")
        user_id = dbModule.save_user(
            email=email,
            username=username,
            firstname=firstname,
            lastname=lastname,
            password=f"{password_hash}:{salt}",  # Store hash and salt together
            dbConnection=dbConnection
        )
        
        success = user_id is not None
        print(f"User registration {'successful' if success else 'failed'} for {username}")
        return success
            
    except Exception as e:
        print(f"Registration error in storeUser: {str(e)}")
        return False