import re

pdf_path = r"C:\Users\Alan\Downloads\Practica 07 - Menú, Toolbars y Material Design.pdf"
with open(pdf_path, "rb") as f:
    content = f.read()

# Extract strings inside parentheses that look like text
strings = re.findall(b'\\((.*?)\\)', content)
text_lines = []
for s in strings:
    try:
        decoded = s.decode('utf-8', errors='ignore')
        if len(decoded) > 3 and any(c.isalnum() for c in decoded):
            text_lines.append(decoded)
    except:
        pass

print(f"Found {len(text_lines)} text strings.")
for line in text_lines:
    print(line)
