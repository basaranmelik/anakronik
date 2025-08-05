from pathlib import Path
from langchain_community.document_loaders import PyPDFLoader
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_community.vectorstores import Qdrant
from qdrant_client.http.models import Distance, VectorParams
from config.qdrant_client import client, EMBEDDING_MODEL,QDRANT_HOST,QDRANT_PORT

# Yüklenen pdfi chunklara ayırıp vectorestorea kaydeder

def ensure_collection(collection_name):
    existing = [c.name for c in client.get_collections().collections]
    if collection_name not in existing:
        client.create_collection(
            collection_name=collection_name,
            vectors_config=VectorParams(size=768, distance=Distance.COSINE),
        )

def load_and_ingest_pdf(pdf_path: Path, collection_name: str, metadata: dict = None):
    loader = PyPDFLoader(str(pdf_path))
    pages = loader.load_and_split()
    splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=100)
    texts = splitter.split_documents(pages)

    for doc in texts:
        doc.metadata.update(metadata or {})

    ensure_collection(collection_name)

    Qdrant.from_documents(
        documents=texts,
        embedding=EMBEDDING_MODEL,
        collection_name=collection_name,
        url=f"http://{QDRANT_HOST}:{QDRANT_PORT}",
    )
