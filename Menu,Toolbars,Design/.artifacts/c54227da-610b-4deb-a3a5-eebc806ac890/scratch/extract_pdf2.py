import os, zlib, re

pdf_path = r"C:\Users\Alan\Downloads\Practica 07 - Menú, Toolbars y Material Design.pdf"
with open(pdf_path, "rb") as f:
    content = f.read()

# Find all text between BT and ET or extract streams
print("PDF size:", len(content))

# Let's search for stream objects and decompress them
streams = re.findall(b'<<.*?>>\\s*stream\\s*(.*?)\\s*endstream', content, re.DOTALL)
text_chunks = []
for s in streams:
    try:
        decompressed = zlib.decompress(s)
        # extract strings inside parentheses ( ... )
        strings = re.findall(b'\\((.*?)\\)', decompressed)
        for st in strings:
            try:
                text_chunks.append(st.decode('latin1'))
            except:
                pass
    except:
        pass

if not text_chunks:
    # try uncompressed or simple regex for text in parentheses
    strings = re.findall(b'\\((.*?)\\)', content)
    for st in strings:
        try:
            text_chunks.append(st.decode('latin1'))
        except:
            pass

print("Extracted text chunks count:", len(text_chunks))
print("Sample text:")
for t in text_chunks[:100]:
    print(t)
