import urllib.request
import subprocess
import sys

url = "https://bootstrap.pypa.io/get-pip.py"
print("Downloading get-pip.py...")
response = urllib.request.urlopen(url)
get_pip_code = response.read()

with open("get-pip.py", "wb") as f:
    f.write(get_pip_code)

print("Running get-pip.py...")
subprocess.run([sys.executable, "get-pip.py"])
print("Pip installation complete.")
