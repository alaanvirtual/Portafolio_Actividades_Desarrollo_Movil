import re, zlib

pdf_path = r"C:\Users\Alan\Downloads\Practica 07 - Menú, Toolbars y Material Design.pdf"
with open(pdf_path, "rb") as f:
    content = f.read()

streams = re.findall(b'<<.*?>>\\s*stream\\s*(.*?)\\s*endstream', content, re.DOTALL)
print(f"Total streams found: {len(streams)}")

all_text = []
for idx, s in enumerate(streams):
    try:
        decompressed = zlib.decompress(s)
        # find strings in parentheses
        matches = re.findall(b'\\((.*?)\\)', decompressed)
        for m in matches:
            try:
                decoded = m.decode('latin1')
                all_text.append(decoded)
            except:
                pass
    except:
        pass

print(f"Extracted {len(all_text)} strings from streams.")
with open(r"C:\Users\Alan\AndroidStudioProjects\Portafolio_Practicas\Menu,Toolbars,Design\.artifacts\c54227da-610b-4deb-a3a5-eebc806ac890\scratch\pdf_strings.txt", "w", encoding="utf-8") as out:
    for t in all_text:
        out.write(t + "\n")

print("Saved to pdf_strings.txt")
