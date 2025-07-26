from config.qdrant_client import client, EMBEDDING_MODEL

def list_collections():
    collections = client.get_collections().collections
    return {"collections": [col.name for col in collections]}

def get_collection_stats(collection_name: str):
    try:
        stats = client.get_collection(collection_name).points_count
        return {"collection": collection_name, "vector_count": stats}
    except Exception as e:
        return {"error": str(e)}

def query_collection(collection_name: str, query_text: str, top_k: int = 3):
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

def delete_collection(collection_name: str):
    try:
        client.delete_collection(collection_name=collection_name)
        return {"status": "ok", "message": f"'{collection_name}' koleksiyonu silindi."}
    except Exception as e:
        return {"error": str(e)}
