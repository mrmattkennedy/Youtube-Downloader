import glob
import os

dir_path = os.path.dirname(os.path.realpath(__file__))
files_path = os.path.join(dir_path, '*')
newest = sorted(glob.iglob(files_path), key=os.path.getctime, reverse=True)[0] 

print(os.path.getsize(newest))
f = open(newest, 'rb')

l = f.read(1024)
print(l)
l = f.read(1024)
print(l)
