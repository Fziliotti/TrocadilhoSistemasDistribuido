import os
from threading import Thread
from time import sleep


class Th_cliente(Thread):

    def __init__(self, num):
        Thread.__init__(self)
        self.num = num

    def run(self):
        print("starting client " + str(self.num))
        os.system("mvn exec:java -Dexec.mainClass=\"trocadilho.server.ServerGRPC\"")
        os.system("1")
        os.system("2")
        os.system("user" + self.num)
        os.system("trocadilho do user" + self.num)


class Th(Thread):

    def __init__(self, num):
        Thread.__init__(self)
        self.num = num

    def run(self):
        print("starting server " +str(self.num))
        os.system("mvn exec:java -Dexec.mainClass=\"trocadilho.server.ServerGRPC\"")


servers = []
for i in range(16):
    servers.append(Th(i))
    servers[i].start()

clients = []
sleep(4)
for i in range(10):
    clients.append(Th_cliente(i))
    clients[i].start()
