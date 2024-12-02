import math
import database.databaseModule as dbModule
import configparser
import json
import os
import utils.Response as Response
def sendPosts(jsonFile, connection, dbConnection):
    print("send posts in range of:", jsonFile)
    config = configparser.ConfigParser()
    config.read(os.path.join(os.path.dirname(__file__),'../../config.ini'))
    posts_range_km = jsonFile.get("range", int(config.get("General", "posts_range")))
    latitude = jsonFile["latitude"]
    longitude = jsonFile["longitude"]
    lowLat, highLat = getLatRange(latitude, posts_range_km)
    lowLong, highLong = getLongRange(longitude, latitude, posts_range_km)
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
        print(postlist)
        try:
            postObj = {
                "id": post[0],
                "userID": post[1],
                "name": post[2],
                "content": post[3],
                "latitude": post[4],
                "longitude": post[5],
                "imageLink": post[6],
                "username": post[7],
                "likes": post[8],
                "datetime": post[9],
                "isAnon": bool(post[10])
            }
            postObjList.append(postObj)
        except IndexError as e:
            print("IndexError parsing postlist in sendPosts:", e)
        except Exception as e:
            print("Error parsing postlist in sendPosts:", e)
    return postObjList

def getLatRange(latitude, km):
    degreeRange = km / 111
    lowLat = latitude - degreeRange
    highLat = latitude + degreeRange
    return round(lowLat,8), round(highLat,8)

def getLongRange(longitude, latitude, km):
    degreeRange = km / (111 * math.cos(math.radians(latitude)))
    lowLong = longitude - degreeRange
    highLong = longitude + degreeRange
    return round(lowLong, 8), round(highLong, 8)