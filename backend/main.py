from fastapi import FastAPI, UploadFile, Form, File, Query
from typing import List
import shutil
from pathlib import Path
from ingestion import load_and_ingest_pdf, client, EMBEDDING_MODEL
from qdrant_client.http.models import SearchRequest, PointStruct
from langchain_core.documents import Document

app = FastAPI()


@app.post("/upload/")
async def upload_character_pdf(
    user_id: str = Form(...),
    character_name: str = Form(...),
    file: UploadFile = File(...)
):
    user_folder = Path(f"uploads/{user_id}/{character_name}")
    user_folder.mkdir(parents=True, exist_ok=True)

    file_path = user_folder / file.filename
    with open(file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    load_and_ingest_pdf(
        pdf_path=file_path,
        collection_name=f"{user_id}_{character_name}",
        metadata={
            "user": user_id,
            "character": character_name,
            "source": "user_upload"
        }
    )
    return {"status": "ok", "message": "PDF başarıyla yüklendi."}


@app.get("/collections/")
def list_collections():
    collections = client.get_collections().collections
    return {"collections": [col.name for col in collections]}


@app.get("/collection/{collection_name}/stats/")
def get_collection_stats(collection_name: str):
    try:
        stats = client.get_collection(collection_name).points_count
        return {"collection": collection_name, "vector_count": stats}
    except Exception as e:
        return {"error": str(e)}


@app.get("/query/")
def query_collection(
    collection_name: str = Query(...),
    query_text: str = Query(...),
    top_k: int = Query(3)
):
    try:
        embedding = EMBEDDING_MODEL.embed_query(query_text)
        hits = client.search(
            collection_name=collection_name,
            query_vector=embedding,
            limit=top_k
        )
        results = [
            {
                "score": hit.score,
                "payload": hit.payload
            }
            for hit in hits
        ]
        return {"query": query_text, "results": results}
    except Exception as e:
        return {"error": str(e)}
