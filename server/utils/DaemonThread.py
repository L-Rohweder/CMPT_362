from threading import Thread

class DaemonThread:
    def __init__(self,target,args = [], threadList = None):
                workerThread = Thread(target = target, args = args)
                workerThread.daemon = True
                workerThread.start()
                if threadList != None: threadList.append(workerThread)