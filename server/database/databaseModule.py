def savePost(name, content, latitude, longitude, imageLink, userID, username, dbConnection):
    cursor = dbConnection.cursor()
    try:
        cursor.execute('''
            INSERT INTO posts (name, content, latitude, longitude, image_link, user_id, username) VALUES (?, ?, ?, ?, ?, ?, ?)
        ''', (name, content, latitude, longitude, imageLink, userID, username))
        dbConnection.commit()
    except Exception as e:
        print("error saving in databaseModule: ", e)

def getPostsInRange(lowLat, highLat, lowLong, highLong, dbConnection):
    cursor = dbConnection.cursor()
    #print(f"lat: {lowLat}-{highLat}, long: {lowLong}-{highLong}")
    cursor.execute("""SELECT * FROM posts 
                   WHERE latitude > ? AND latitude < ?
                   AND longitude > ? AND longitude < ?""",
                   (lowLat,highLat, lowLong, highLong))
    return cursor.fetchall()

def getAllPosts(dbConnection):
    cursor = dbConnection.cursor()
    cursor.execute("SELECT * FROM posts")
    rows = cursor.fetchall()
    return rows

def printAllPosts(dbConnection):
    cursor = dbConnection.cursor()
    cursor.execute("SELECT * FROM posts")
    rows = cursor.fetchall()
    print("All posts:")
    for row in rows:
        print(row)

def save_user(email, username, firstname, lastname, password, dbConnection):
    cursor = dbConnection.cursor()
    try:
        cursor.execute('''
            INSERT INTO users (email, username, firstname, lastname, password)
            VALUES (?, ?, ?, ?, ?)
        ''', (email, username, firstname, lastname, password))
        dbConnection.commit()
        return cursor.lastrowid
    except Exception as e:
        print("Error saving user:", e)
        dbConnection.rollback()
        return None

def get_user_by_username(username, dbConnection):
    cursor = dbConnection.cursor()
    try:
        cursor.execute('''
            SELECT id, email, username, firstname, lastname, gender, bio, 
                   profile_image, password, created_at
            FROM users
            WHERE username = ?
        ''', (username,))
        return cursor.fetchone()
    except Exception as e:
        print("Error getting user:", e)
        return None
