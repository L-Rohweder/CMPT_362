import database.databaseModule as dbModule
def storeReply(jsonFile, dbConnection):
    name = jsonFile["name"]
    content = jsonFile["content"]
    print("saving:")
    print("""   
            name:      {}
            content:   {}
          """.format(name, content))

    dbModule.saveReply(name, content, dbConnection)