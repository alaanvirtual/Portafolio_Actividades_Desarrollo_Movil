import sys
sys.path.insert(0, 'pypdf_lib')
import os
for root, dirs, files in os.walk('pypdf_lib'):
    if 'pypdf' in dirs:
        sys.path.insert(0, root)
        break

import pypdf
reader = pypdf.PdfReader(r'C:\Users\Alan\Downloads\Practica 06 - LazyColumn y Listas Dinámicas.pdf')
out_dir = r'C:\Users\Alan\AndroidStudioProjects\Portafolio_Practicas\LazyColumn\.artifacts\84ec5c97-7224-4916-bfbd-ba4b64c60d3d\scratch\extracted_imgs'
os.makedirs(out_dir, exist_ok=True)

img_count = 0
for i, page in enumerate(reader.pages):
    for count, image_file_object in enumerate(page.images):
        img_count += 1
        img_path = os.path.join(out_dir, f"page_{i+1}_img_{count}_{image_file_object.name}")
        with open(img_path, "wb") as fp:
            fp.write(image_file_object.data)
        print(f"Saved {img_path}")

print(f"Total images extracted: {img_count}")
