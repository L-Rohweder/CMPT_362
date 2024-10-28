def savePost(name, content, latitude, longitude, dbConnection):
    cursor = dbConnection.cursor()
    try:
        cursor.execute('''
            INSERT INTO posts (name, content, latitude, longitude) VALUES (?, ?, ?, ?)
        ''', (name, content, latitude, longitude))
        dbConnection.commit()
    except Exception as e:
        print("error saving in databaseModule: ", e)

def printAllPosts(dbConnection):
    cursor = dbConnection.cursor()
    cursor.execute("SELECT * FROM posts")
    rows = cursor.fetchall()
    print("All posts:")
    for row in rows:
        print(row)
