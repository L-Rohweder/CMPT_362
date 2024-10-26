from utils.DaemonThread import DaemonThread
import configparser
import socket

class Server:
    def __init__(self):
        self.config = configparser.ConfigParser()
        self.config.read('config.ini')
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

    def stop(self):
        self.server_socket.shutdown(socket.SHUT_RDWR)
        self.server_socket.close()
        self.running = False
