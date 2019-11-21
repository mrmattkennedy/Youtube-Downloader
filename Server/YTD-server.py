#!/usr/bin/python

import glob
import os
import sys
import socket
import subprocess

def execute(cmd):
    popen = None
    if sys.platform == 'linux':
         popen = subprocess.Popen(cmd, stdout=subprocess.PIPE, universal_newlines=True)
    elif sys.platform == 'win32':
         popen = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, universal_newlines=True)

    for stdout_line in iter(popen.stdout.readline, ""):
        yield stdout_line 
    popen.stdout.close()
    return_code = popen.wait()
    if return_code:
        raise subprocess.CalledProcessError(return_code, cmd)

def send_file():
    name = socket.gethostname()
    host = ''
    port = 12345
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.bind((host, port))
    s.listen(5)
    while True:
        #open socket
        c, addr = s.accept()
        print("Got connection from Matt's Computer!")

        #Buffer to read incoming URL
        buffer = b''
        while True:
            data = c.recv(1)
            if not data or data.decode('utf-8').endswith('\n'):
                break
            buffer += data
        url = buffer.decode('utf-8')

        #Confirm the URL was received with status 200
        c.send('200'.encode('utf-8'))

        #Start sending progress ints
        #exit status 255 = bad
        try:
            for path in execute(["youtube-dl", url]):
                if path.find('returned non-zero exit status 255') != -1:
                    raise Exception
                if path.find('has already been downloaded') != -1:
                    c.send('100'.encode('utf-8'))
                    break
                if path.find('%') == -1:
                    continue
                if path[len('[download] '):path.find('%')] == '100':
                    break
                
                print(path, end='')
                c.send(path[len('[download] '):path.find('%')].encode('utf-8'))
        except Exception as error:
            c.send('255'.encode('utf-8'))
            c.close()
            return
            
        dir_path = os.path.dirname(os.path.realpath(__file__))
        files_path = os.path.join(dir_path, '*')
        newest = sorted(glob.iglob(files_path), key=os.path.getctime, reverse=True)[0]
        
        f = open(newest, 'rb')
        l = f.read()
        c.send(str(len(l)).encode('utf-8'))
        print(str(len(l)))
        c.send(l)
        print('file sent')

        c.close()
        f.close()

send_file()
