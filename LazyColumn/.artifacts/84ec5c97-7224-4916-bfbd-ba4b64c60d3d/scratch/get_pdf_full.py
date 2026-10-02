import sys
sys.path.insert(0, 'pypdf_lib')
import os
for root, dirs, files in os.walk('pypdf_lib'):
    if 'pypdf' in dirs:
        sys.path.insert(0, root)
        break

import pypdf
reader = pypdf.PdfReader(r'C:\Users\Alan\Downloads\Practica 06 - LazyColumn y Listas Dinámicas.pdf')
out_path = r'C:\Users\Alan\AndroidStudioProjects\Portafolio_Practicas\LazyColumn\.artifacts\84ec5c97-7224-4916-bfbd-ba4b64c60d3d\scratch\pdf_text.txt'
with open(out_path, 'w', encoding='utf-8') as f:
    for i, page in enumerate(reader.pages):
        f.write(f"=== PAGE {i+1} ===\n")
        f.write(page.extract_text() + "\n\n")
print("Done writing pdf_text.txt")
