import database.databaseModule as dbModule
import configparser
import json
import os
import utils.Response as Response
def sendPosts(jsonFile, connection, dbConnection):
    print("send posts in range of:", jsonFile)
    config = configparser.ConfigParser()
    config.read(os.path.join(os.path.dirname(__file__),'../../config.ini'))
    range = int(config.get("General", "posts_range"))
    latitude = jsonFile["latitude"]
    longitude = jsonFile["longitude"]
    lowLat, highLat = getLatRange(latitude, range)
    lowLong, highLong = getLongRange(longitude, range)
    postlist = dbModule.getPostsInRange(lowLat, highLat, lowLong, highLong, dbConnection)
    sendPostList(connection, postlist)

def sendAllPosts(connection, dbConnection):
    print("send all")
    postlist = dbModule.getAllPosts(dbConnection)
    sendPostList(connection, postlist)


def sendPostList(connection, postlist):
    response = json.dumps(postListListToPostObjectList(postlist))
    connection.sendall(Response.OKBODY(response).encode('utf-8'))

def postListListToPostObjectList(postlist):
    postObjList = []
    for post in postlist:
        postObj = {}
        try:
            postObj["name"] = post[0]
            postObj["content"] = post[1]
            postObj["latitude"] = post[2]
            postObj["longitude"] = post[3]
            postObj["datetime"] = post[4]
            postObjList.append(postObj)
        except:
            print("error parsing postlist in sendPosts")
    return postObjList

def getLatRange(latitude, km):
    degreeRange = km / 111
    lowLat = latitude - degreeRange
    highLat = latitude + degreeRange
    return round(lowLat,8), round(highLat,8)

def getLongRange(longitude, km):
    degreeRange = km / 111
    lowLong = longitude - degreeRange
    highLong = longitude + degreeRange
    return lowLong, highLong