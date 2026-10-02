import re
import zlib

pdf_path = r'C:\Users\Alan\Downloads\Practica 06 - LazyColumn y Listas Dinámicas.pdf'
with open(pdf_path, 'rb') as f:
    content = f.read()

streams = re.findall(b'stream\r?\n(.*?)\r?\nendstream', content, re.DOTALL)
print(f"Found {len(streams)} streams.")
full_text = []

for s in streams:
    try:
        dec = zlib.decompress(s)
        # Try to find text in parentheses
        matches = re.findall(b'\(([^)]+)\)', dec)
        for m in matches:
            try:
                full_text.append(m.decode('latin1'))
            except:
                pass
    except Exception as e:
        pass

# Also search raw content
matches_raw = re.findall(b'\(([^)]+)\)', content)
for m in matches_raw:
    try:
        full_text.append(m.decode('latin1'))
    except:
        pass

print("Extracted text chunks:")
for line in full_text:
    print(line)
