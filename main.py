from pinecone import Pinecone, ServerlessSpec
import os 
import json
from Loader.PDFLoader import PDFLoader

# initialization
def initialize(index_name: str) -> Pinecone: 
    pc = Pinecone(api_key = "pcsk_4h95E2_AQKncKY255Ak1Ri9dbGRoZb4YZXmDCGwttocZRRJv6MhCTG7k3eob2844mtKZHT")
    

    if not pc.has_index(index_name):
        pc.create_index_for_model(
            name=index_name,
            cloud="aws",
            region="us-east-1",
            embed={
                "model":"llama-text-embed-v2",
                "field_map":{"text": "chunk_text"}
            }
        )
    
    return pc

if __name__ == "__main__":
    index_name = "developer-quickstart-py"
    pc = initialize(index_name)


    loader = PDFLoader("/Users/erykmaushagen/energy-rag/dataset/EnWG.pdf")

    loader.extract_text()
    
    records = loader.text_to_record()
    
    print(records[:5]) 
    print(f"Anzahl Records: {len(records)}")


    # dense_index = pc.Index(index_name)
    # dense_index.upsert_records("namespace", records)