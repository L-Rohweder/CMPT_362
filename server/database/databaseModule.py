import json

def savePost(name, content, latitude, longitude, imageLink, userID, username, isAnon, dbConnection):
    cursor = dbConnection.cursor()
    try:
        cursor.execute('''
            INSERT INTO posts (user_id, name, content, latitude, longitude, image_link, username, is_anon, liked_user_ids, disliked_user_ids) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (userID,name, content, latitude, longitude, imageLink or "", username, isAnon, json.dumps([]), json.dumps([])))
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

def getLikes(postId, dbConnection):
    cursor = dbConnection.cursor()
    try:
        cursor.execute("SELECT liked_user_ids FROM posts WHERE id = ?",(postId,))
        return cursor.fetchall()[0][0]
    except Exception as e:
        print("error liking in databaseModule: ", e)

def likePost(postId, userId, dbConnection):
    cursor = dbConnection.cursor()
    try:
        cursor.execute("SELECT liked_user_ids, disliked_user_ids FROM posts WHERE id = ?", (postId,))
        row = cursor.fetchone()
        liked_user_ids = json.loads(row[0]) if row[0] else []
        disliked_user_ids = json.loads(row[1]) if row[1] else []
        new_user_id = str(userId)
        if new_user_id in liked_user_ids:
            liked_user_ids.remove(new_user_id)
        else:
            liked_user_ids.append(new_user_id)
        if new_user_id in disliked_user_ids:
            disliked_user_ids.remove(new_user_id)
        if row:
            cursor.execute("""
                UPDATE posts
                SET liked_user_ids = ?, disliked_user_ids = ?
                WHERE id = ?;
                """, (json.dumps(liked_user_ids), json.dumps(disliked_user_ids), postId))
            dbConnection.commit()
            return (json.dumps(liked_user_ids), json.dumps(disliked_user_ids))
        else:
            raise Exception("Cannot like, Post does not exist")
    except Exception as e:
        print("error like in databaseModule: ", e)

def dislikePost(postId, userId, dbConnection):
    cursor = dbConnection.cursor()
    try:
        cursor.execute("SELECT liked_user_ids, disliked_user_ids FROM posts WHERE id = ?", (postId,))
        row = cursor.fetchone()
        liked_user_ids = json.loads(row[0]) if row[0] else []
        disliked_user_ids = json.loads(row[1]) if row[1] else []
        new_user_id = str(userId)
        if new_user_id in liked_user_ids:
            liked_user_ids.remove(new_user_id)
        if new_user_id in disliked_user_ids:
            disliked_user_ids.remove(new_user_id)
        else:
            disliked_user_ids.append(new_user_id)
        if row:
            cursor.execute("""
                UPDATE posts
                SET liked_user_ids = ?, disliked_user_ids = ?
                WHERE id = ?;
                """, (json.dumps(liked_user_ids), json.dumps(disliked_user_ids), postId))
            dbConnection.commit()
            return (json.dumps(liked_user_ids), json.dumps(disliked_user_ids))
        else:
            raise Exception("Cannot Dislike, Post does not exist")
    except Exception as e:
        print("error disliking in databaseModule: ", e)

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
            SELECT *
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
