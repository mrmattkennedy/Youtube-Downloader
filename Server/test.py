import glob
import os
import subprocess

dir_path = os.path.dirname(os.path.realpath(__file__))
files_path = os.path.join(dir_path, '*')
newest = sorted(glob.iglob(files_path), key=os.path.getctime, reverse=True)[0] 

def execute(cmd):
    popen = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, universal_newlines=True)
    for stdout_line in iter(popen.stdout.readline, ""):
        yield stdout_line 
    popen.stdout.close()
    return_code = popen.wait()
    if return_code:
        raise subprocess.CalledProcessError(return_code, cmd)

# Example
for path in execute(["youtube-dl", "https://www.youtube.com/watch?v=YQHsXMglC9A"]):
    #print(path[len('[download] '):path.find('%')])
    print(path, end="")

