import database.databaseModule as dbModule
def storePost(jsonFile, dbConnection):
    name = jsonFile["name"]
    content = jsonFile["content"]
    latitude = jsonFile["latitude"]
    longitude = jsonFile["longitude"]
    imageLink = jsonFile["imageLink"]
    userID = jsonFile["userID"]
    username = jsonFile["username"]
    print("saving:")
    print("""   
            name:      {}
            content:   {}
            latitude:  {}
            longitude: {}
            imageLink: {}
            userID:    {}
            username:  {}
          """.format(name, content, latitude, longitude, imageLink, userID, username))

    dbModule.savePost(name, content, float(latitude), float(longitude), imageLink, userID, username, dbConnection)