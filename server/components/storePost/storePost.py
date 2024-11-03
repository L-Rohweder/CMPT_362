import database.databaseModule as dbModule
def storePost(jsonFile, dbConnection):
    name = jsonFile["name"]
    content = jsonFile["content"]
    latitude = jsonFile["latitude"]
    longitude = jsonFile["longitude"]
    print("saving:")
    print("""   
            name:      {}
            content:   {}
            latitude:  {}
            longitude: {}
          """.format(name, content, latitude, longitude))

    dbModule.savePost(name, content, float(latitude), float(longitude), dbConnection)