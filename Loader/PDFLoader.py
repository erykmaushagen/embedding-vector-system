
import fitz
import uuid
import re


class PDFLoader: 
    def __init__(self, pdf_path):
        self.pdf_path = pdf_path
        self.text = ""
        self.chunks = []
        self.records = []

    def chunk_by_paragraph(self): 
        """
        Splits text at every  §-sign into chunks.
        """
        # Regex: § gefolgt von beliebigem Text bis zum nächsten § oder Ende
        pattern = r"(§\s*\d+[a-z]*.*?)(?=§\s*\d+[a-z]*|$)"
        matches = re.findall(pattern, self.text, flags=re.DOTALL)

        for match in matches:
            chunk = match.strip()
            if chunk:
                self.chunks.append(chunk)
    
    def extract_text(self) -> str: 
        doc = fitz.open(self.pdf_path)
        for page in doc: 
           self.text += page.get_text()
        return self.text 

    def text_to_record(self, category = False): 
        """
        Split text into chunks and formate them. 
        chunk_size = number of signs per chunk
        """
        self.chunk_by_paragraph()

        # for i in range(0, len(self.text), self.chunk_size):
        #     chunk = self.text[i:i+self.chunk_size].strip()
        #     if chunk:
        #         self.chunks.append(chunk)
        
        id = 1
        for chunk in self.chunks:
            record = {
                "_id": "rec" + str(id),  # eindeutige ID
                "chunk_text": chunk,
                "category": category  # optional
            }
            id += 1
            self.records.append(record)
        print("************************************test************************************")
        return self.records

