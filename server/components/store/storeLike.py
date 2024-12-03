from database.databaseModule import likePost
def storeLike(jsonfile, db_connection):
    userId = jsonfile["userID"]
    postId = jsonfile["postID"]
    likePost(postId, userId, db_connection)
    print("liking post: "+str(postId) + "with id: "+str(userId))
