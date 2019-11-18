#!/usr/bin/python

import socket

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

name = socket.gethostname()
host = socket.gethostbyname(name)
host = ''
print(name)
print(host)
port = 12345
s.bind((host, port))
print(socket.gethostname())
s.listen(5)
while True:
    c, addr = s.accept()
    print("Got connection from Matt's Computer!")
    data = c.recv(1024)
    print(data)
    c.close()
