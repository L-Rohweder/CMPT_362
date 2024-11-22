def savePost(name, content, latitude, longitude, imageLink, dbConnection):
    cursor = dbConnection.cursor()
    try:
        cursor.execute('''
            INSERT INTO posts (name, content, latitude, longitude, image_link) VALUES (?, ?, ?, ?, ?)
        ''', (name, content, latitude, longitude, imageLink))
        dbConnection.commit()
    except Exception as e:
        print("error saving in databaseModule: ", e)

def saveReply(postId, name, content, dbConnection):
    cursor = dbConnection.cursor()
    try:
        cursor.execute('''
            INSERT INTO replies (postId, name, content) VALUES (?, ?, ?)
        ''', (postId, name, content))
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

def getRepliesFromPost(postId, dbConnection):
    cursor = dbConnection.cursor()
    cursor.execute("""SELECT * FROM replies 
                   WHERE postId = ?""",
                   (postId,))
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
