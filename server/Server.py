import sqlite3
from utils.DaemonThread import DaemonThread
from components.sendPosts.sendPosts import sendPosts
from components.storePost.storePost import storePost
import utils.Response as Response
import configparser
import socket
import os
import json

class Server:
    def __init__(self):
        self.config = configparser.ConfigParser()
        self.config.read('config.ini')
        self.db_path = os.path.join("database", self.config.get("Database", "db_name"))
        self.db_connection = sqlite3.connect(self.db_path, check_same_thread=False)
        self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.server_address = ('0.0.0.0', int(self.config.get('General','server_port')))
        self.server_socket.bind(self.server_address)
        self.server_socket.listen(1)
        self.running = True

    def start(self):
        print("server started serving on port: "+ self.config.get('General','server_port'))
        while self.running:
            print("waiting for connection")
            try:
                connection, client_address = self.server_socket.accept()
                DaemonThread(target = self.processConnection, args = (connection, client_address))
            except Exception as e:
                print("Connection Failed with error:" + e)
    
    def processConnection(self, connection, client_address):
        print("Connection made with"+str(client_address))
        message = connection.recv(1024).decode()
        headers, body = message.split("\r\n\r\n", 1)
        string_list = headers.split(" ")
        endpoint = string_list[1][1:] #gets request type and removes '/'
        try:
            print(endpoint)
            jsonfile = json.loads(body)
            self.passToComponent(endpoint, jsonfile, connection)
        except json.JSONDecodeError as e:
            print("json processing failed", e)
    
    def passToComponent(self, endpoint, jsonfile, connection):
        match endpoint:
            case 'get':
                sendPosts(jsonfile, connection, self.db_connection)
            case 'post':
                connection.sendall(Response.OK().encode('utf-8'))
                connection.close()
                storePost(jsonfile, self.db_connection)


    def stop(self):
        self.server_socket.shutdown(socket.SHUT_RDWR)
        self.server_socket.close()
        self.running = False
