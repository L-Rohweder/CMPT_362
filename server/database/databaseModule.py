def savePost(name, content, latitude, longitude, imageLink, userID, username, isAnon, dbConnection):
    cursor = dbConnection.cursor()
    try:
        cursor.execute('''
            INSERT INTO posts (name, content, latitude, longitude, image_link, user_id, username, is_anon) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        ''', (name, content, latitude, longitude, imageLink or "", userID, username, isAnon))
        dbConnection.commit()
    except Exception as e:
        print("error saving in databaseModule: ", e)

def saveReply(postId, name, content, userId, isAnon, dbConnection):
    cursor = dbConnection.cursor()
    try:
        cursor.execute('''
            INSERT INTO replies (post_id, name, content, user_id, is_anon) VALUES (?, ?, ?, ?, ?)
        ''', (postId, name, content, userId, isAnon))
        dbConnection.commit()
    except Exception as e:
        print("error saving in databaseModule: ", e)

def getPostsInRange(lowLat, highLat, lowLong, highLong, dbConnection):
    cursor = dbConnection.cursor()
    try:
        cursor.execute("""
            SELECT *
            FROM posts 
            WHERE latitude > ? AND latitude < ?
            AND longitude > ? AND longitude < ?
        """, (lowLat, highLat, lowLong, highLong))
        return cursor.fetchall()
    except Exception as e:
        print("Error getting posts in range:", e)
        return []

def getRepliesFromPost(postId, dbConnection):
    cursor = dbConnection.cursor()
    cursor.execute("""SELECT * FROM replies 
                   WHERE post_id = ?""",
                   (postId,))
    return cursor.fetchall()

def getAllPosts(dbConnection):
    cursor = dbConnection.cursor()
    try:
        cursor.execute("""
            SELECT name, content, latitude, longitude, image_link, user_id, username, created_at 
            FROM posts
        """)
        return cursor.fetchall()
    except Exception as e:
        print("Error getting all posts:", e)
        return []

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
