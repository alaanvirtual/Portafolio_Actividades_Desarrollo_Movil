import sys

pdf_path = r"C:\Users\Alan\Downloads\Practica 07 - Menú, Toolbars y Material Design.pdf"
try:
    import pypdf
    reader = pypdf.PdfReader(pdf_path)
    for i, page in enumerate(reader.pages):
        print(f"=== PAGE {i+1} ===")
        print(page.extract_text())
except Exception as e:
    print("pypdf error:", e)
