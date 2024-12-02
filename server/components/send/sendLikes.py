from database.databaseModule import getLikes
import json
import utils.Response as Response
def sendLikes(jsonfile, connection, db_connection):
    postId = jsonfile["postID"]
    likeList = getLikes(postId, db_connection)
    sendLikeList(connection, likeList)

def sendLikeList(connection, likeList):
    response = json.dumps(likeList)
    response = response.replace("\\", '')
    response = response[1:-1]
    connection.sendall(Response.OKBODY(response).encode('utf-8'))