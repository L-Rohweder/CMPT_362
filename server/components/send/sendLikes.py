from database.databaseModule import getLikes
import json
import utils.Response as Response
def sendLikes(jsonfile, connection, db_connection):
    postId = jsonfile["postId"]
    likeList = getLikes(postId, db_connection)
    sendLikeList(connection, likeList)

def sendLikeList(connection, likeList):
    response = json.dumps(likeListToLikeObject(likeList))
    connection.sendall(Response.OKBODY(response).encode('utf-8'))

def likeListToLikeObject(likeList):
    try:
        likeObj = {
            "likes": likeList[0],
        }
        return likeObj
    except Exception as e:
        print("Error parsing postlist in sendPosts:", e)