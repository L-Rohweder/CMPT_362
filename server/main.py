from Server import Server
from utils.DaemonThread import DaemonThread

if __name__=="__main__":
    s = Server()
    DaemonThread(s.start)

    running = True
    while running:
        user_input = input("input 'stop' to stop serving\n")
        if user_input == 'stop':
            running = False

    s.stop()