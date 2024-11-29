import database.databaseModule as dbModule
def storeReply(jsonFile, dbConnection):
    postId = jsonFile["postId"]
    name = jsonFile["name"]
    content = jsonFile["content"]
    userId = jsonFile["userId"]
    isAnon = jsonFile["isAnon"]
    print("saving:")
    print("""
            postId:    {}
            name:      {}
            content:   {}
            userId:   {}
            isAnon:   {}
          """.format(postId, name, content, userId, isAnon))

    dbModule.saveReply(postId, name, content, userId, isAnon, dbConnection)