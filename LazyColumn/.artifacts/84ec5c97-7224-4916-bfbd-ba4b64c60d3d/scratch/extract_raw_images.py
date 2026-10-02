import sys
sys.path.insert(0, 'pypdf_lib')
import os
for root, dirs, files in os.walk('pypdf_lib'):
    if 'pypdf' in dirs:
        sys.path.insert(0, root)
        break

import pypdf
reader = pypdf.PdfReader(r'C:\Users\Alan\Downloads\Practica 06 - LazyColumn y Listas Dinámicas.pdf')
out_dir = r'C:\Users\Alan\AndroidStudioProjects\Portafolio_Practicas\LazyColumn\.artifacts\84ec5c97-7224-4916-bfbd-ba4b64c60d3d\scratch\extracted_raw_imgs'
os.makedirs(out_dir, exist_ok=True)

img_count = 0
for i, page in enumerate(reader.pages):
    resources = page.get("/Resources")
    if resources:
        xobject = resources.get("/XObject")
        if xobject:
            try:
                xobj = xobject.get_object()
                for obj_name in xobj:
                    obj = xobj[obj_name].get_object()
                    if obj.get("/Subtype") == "/Image":
                        img_count += 1
                        data = obj.get_data()
                        filter_type = obj.get("/Filter")
                        ext = "bin"
                        if filter_type == "/DCTDecode":
                            ext = "jpg"
                        elif filter_type == "/FlateDecode":
                            ext = "png" # or raw
                        img_path = os.path.join(out_dir, f"page_{i+1}_{img_count}_{obj_name.lstrip('/')}.{ext}")
                        with open(img_path, "wb") as f:
                            f.write(data)
                        print(f"Saved {img_path}")
            except Exception as e:
                print(f"Error on page {i+1}: {e}")

print(f"Total raw images extracted: {img_count}")
