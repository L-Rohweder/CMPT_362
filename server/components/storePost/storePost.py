import database.databaseModule as dbModule
def storePost(jsonFile, dbConnection):
    name = jsonFile["name"]
    content = jsonFile["content"]
    latitude = jsonFile["latitude"]
    longitude = jsonFile["longitude"]
    imageLink = jsonFile["imageLink"]
    print("saving:")
    print("""   
            name:      {}
            content:   {}
            latitude:  {}
            longitude: {}
            imageLink: {}
          """.format(name, content, latitude, longitude, imageLink))

    dbModule.savePost(name, content, float(latitude), float(longitude), imageLink, dbConnection)