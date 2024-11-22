import database.databaseModule as dbModule
def storeReply(jsonFile, dbConnection):
    postId = jsonFile["postId"]
    name = jsonFile["name"]
    content = jsonFile["content"]
    print("saving:")
    print("""
            postId:    {}
            name:      {}
            content:   {}
          """.format(postId, name, content))

    dbModule.saveReply(postId, name, content, dbConnection)