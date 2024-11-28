import database.databaseModule as dbModule
def storePost(jsonFile, dbConnection):
    name = jsonFile["name"]
    content = jsonFile["content"]
    latitude = jsonFile["latitude"]
    longitude = jsonFile["longitude"]
    imageLink = jsonFile.get("imageLink", "")
    userID = jsonFile["userID"]
    username = jsonFile["username"]
    isAnon = jsonFile["isAnon"]
    print("saving:")
    print("""   
            name:      {}
            content:   {}
            latitude:  {}
            longitude: {}
            imageLink: {}
            userID:    {}
            username:  {}
            isAnon: {}
          """.format(name, content, latitude, longitude, imageLink, userID, username, isAnon))

    dbModule.savePost(name, content, float(latitude), float(longitude), imageLink, userID, username, isAnon, dbConnection)