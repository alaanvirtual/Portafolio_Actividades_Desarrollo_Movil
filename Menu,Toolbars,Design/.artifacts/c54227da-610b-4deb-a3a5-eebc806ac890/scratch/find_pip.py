import os, sys, subprocess

print("sys.executable:", sys.executable)
paths = [
    r"C:\Program Files\Python311\python.exe",
    r"C:\Program Files\Python310\python.exe",
    r"C:\Program Files\Python39\python.exe",
    r"C:\Users\Alan\AppData\Local\Programs\Python\Python311\python.exe",
    r"C:\Users\Alan\AppData\Local\Programs\Python\Python310\python.exe",
    r"C:\Users\Alan\AppData\Local\Programs\Python\Python39\python.exe",
]
for p in paths:
    if os.path.exists(p):
        print("Found python at:", p)
        try:
            res = subprocess.run([p, "-m", "pip", "--version"], capture_output=True, text=True)
            print("Pip version:", res.stdout)
        except Exception as e:
            print("Pip error:", e)
