#!/usr/bin/python

import glob
import os
import socket
import subprocess

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
    buffer = b''
    while True:
        data = c.recv(1)
        if not data or data.decode('utf-8').endswith('\n'):
            break
        buffer += data
        #print('buffer is ' + buffer.decode('utf-8'))
    url = buffer.decode('utf-8')
    c.send('200'.encode('utf-8'))
    subprocess.call(["youtube-dl", url], shell=True, stdout=subprocess.PIPE)
    dir_path = os.path.dirname(os.path.realpath(__file__))
    files_path = os.path.join(dir_path, '*')
    newest = sorted(glob.iglob(files_path), key=os.path.getctime, reverse=True)[0] 
    f = open(newest, 'rb')
    l = f.read()
    c.send(str(len(l)).encode('utf-8'))
    print(len(l))
    c.send(l)
    print('file sent')
    c.close()
    f.close()
