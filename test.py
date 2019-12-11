import os
from threading import Thread
from time import sleep
class Th_cliente(Thread):

    def __init__ (self, num):
        Thread.__init__(self)
        self.num = num
    def run(self):
        os.system("mvn exec:java")

class Th(Thread):

    def __init__ (self, num):
        Thread.__init__(self)
        self.num = num
    def run(self):
        os.system("python3.7 server.py 2")

b=Th(0)
b.start()
a=[]
sleep(4)
for i in range(10):
    a.append(Th_cliente(i))
    a[i].start()
