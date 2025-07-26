import shutil
from pathlib import Path
from utils.ingestion_loader import load_and_ingest_pdf

async def handle_upload(user_id: str, character_name: str, file):
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
