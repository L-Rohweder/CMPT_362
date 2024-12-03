from database.databaseModule import likePost, dislikePost
import utils.Response as Response
import json

def storeLike(jsonfile, db_connection, connection):
    userId = jsonfile["userID"]
    postId = jsonfile["postID"]
    liked_user_ids, disliked_user_ids = likePost(postId, userId, db_connection)
    response = json.dumps({
    "success": True,
    "likedUserIds": liked_user_ids,
    "dislikedUserIds": disliked_user_ids,
    })
    connection.sendall(Response.OKBODY(response).encode('utf-8'))
    print("liking post: "+str(postId) + " with id: "+str(userId))

def storeDislike(jsonfile, db_connection, connection):
    userId = jsonfile["userID"]
    postId = jsonfile["postID"]
    liked_user_ids, disliked_user_ids = dislikePost(postId, userId, db_connection)
    response = json.dumps({
    "success": True,
    "likedUserIds": liked_user_ids,
    "dislikedUserIds": disliked_user_ids,
    })
    connection.sendall(Response.OKBODY(response).encode('utf-8'))
    print("disliking post: "+str(postId) + " with id: "+str(userId))
