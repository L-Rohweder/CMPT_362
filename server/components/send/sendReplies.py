import math
import database.databaseModule as dbModule
import configparser
import json
import os
import utils.Response as Response
def sendReplies(jsonFile, connection, dbConnection):
    print("send replies from post:", jsonFile)
    config = configparser.ConfigParser()
    config.read(os.path.join(os.path.dirname(__file__),'../../config.ini'))
    postId = jsonFile["postId"]
    replyList = dbModule.getRepliesFromPost(postId, dbConnection)
    sendReplyList(connection, replyList)

def sendReplyList(connection, replyList):
    response = json.dumps(replyListToReplyObjectList(replyList))
    connection.sendall(Response.OKBODY(response).encode('utf-8'))

def replyListToReplyObjectList(replyList):
    replyObjList = []
    for reply in replyList:
        try:
            postObj = {
                "id":reply[0],
                "postId": reply[1],
                "userId": reply[2],
                "name": reply[3],
                "content": reply[4],
                "datetime": reply[5],
                "isAnon": bool(reply[6]),
            }
            replyObjList.append(postObj)
        except IndexError as e:
            print("IndexError parsing replyList in sendReplies:", e)
        except Exception as e:
            print("Error parsing posreplyListtlist in sendReplies:", e)
    return replyObjList
