import os
img_dir = r'C:\Users\Alan\AndroidStudioProjects\Portafolio_Practicas\LazyColumn\.artifacts\84ec5c97-7224-4916-bfbd-ba4b64c60d3d\scratch\extracted_raw_imgs'
for f in os.listdir(img_dir):
    fp = os.path.join(img_dir, f)
    size = os.path.getsize(fp)
    print(f"{f}: {size} bytes")
