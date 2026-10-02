import urllib.request
import json
import tarfile
import io
import zipfile

# Get pypdf download URL from PyPI JSON API
url = "https://pypi.org/pypi/pypdf/json"
req = urllib.request.urlopen(url)
data = json.loads(req.read().decode('utf-8'))
releases = data['releases']
latest_version = data['info']['version']
print(f"Latest pypdf version: {latest_version}")

wheel_url = None
for file_info in releases[latest_version]:
    if file_info['packagetype'] == 'bdist_wheel':
        wheel_url = file_info['url']
        break

if not wheel_url:
    for file_info in releases[latest_version]:
        if file_info['packagetype'] == 'sdist':
            wheel_url = file_info['url']
            break

print(f"Downloading from {wheel_url}")
whl_data = urllib.request.urlopen(wheel_url).read()

# If wheel (zip) or sdist (tar.gz)
if wheel_url.endswith('.whl'):
    z = zipfile.ZipFile(io.BytesIO(whl_data))
    z.extractall('pypdf_lib')
else:
    t = tarfile.open(fileobj=io.BytesIO(whl_data), mode='r:*')
    t.extractall('pypdf_lib')

import sys
sys.path.insert(0, 'pypdf_lib')
# If sdist, find pypdf folder inside extracted dir
import os
for root, dirs, files in os.walk('pypdf_lib'):
    if 'pypdf' in dirs:
        sys.path.insert(0, root)
        break

import pypdf
reader = pypdf.PdfReader(r'C:\Users\Alan\Downloads\Practica 06 - LazyColumn y Listas Dinámicas.pdf')
for i, page in enumerate(reader.pages):
    print(f"=== PAGE {i+1} ===")
    print(page.extract_text())
