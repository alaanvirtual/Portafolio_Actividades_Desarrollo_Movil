import sys
try:
    import pypdf
    reader = pypdf.PdfReader(r"C:\Users\Alan\Downloads\Practica 07 - Menú, Toolbars y Material Design.pdf")
    for i, page in enumerate(reader.pages):
        print(f"--- PAGE {i+1} ---")
        print(page.extract_text())
except Exception as e:
    print("Error with pypdf:", e)
    try:
        import fitz
        doc = fitz.open(r"C:\Users\Alan\Downloads\Practica 07 - Menú, Toolbars y Material Design.pdf")
        for i, page in enumerate(doc):
            print(f"--- PAGE {i+1} ---")
            print(page.get_text())
    except Exception as e2:
        print("Error with PyMuPDF:", e2)
