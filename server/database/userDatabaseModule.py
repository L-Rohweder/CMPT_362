
def saveUser(id, email, username, firstname, lastname, gender, bio, profileImage, password, dbConnection):
    cursor = dbConnection.cursor()
    try:
        cursor.execute('''
            INSERT INTO users (id, email, username, firstname, lastname, gender, bio, profile_image, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (id, email, username, firstname, lastname, gender, bio, profileImage, password))
        dbConnection.commit()
    except Exception as e:
        print("error saving in databaseModule: ", e)