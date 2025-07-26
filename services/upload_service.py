import shutil
import json
import re
from pathlib import Path
from utils.ingestion_loader import load_and_ingest_pdf
from langchain_community.document_loaders import PyPDFLoader
from agents.character_info_extractor import character_info_extraction_chain
from services.query_service import query_collection

async def handle_upload(user_id: str, character_name: str, file):
    try:
        # PDF dosyasını kaydet
        user_folder = Path(f"uploads/{user_id}/{character_name}")
        user_folder.mkdir(parents=True, exist_ok=True)
        file_path = user_folder / file.filename

        with open(file_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)

        # Vektör verisini Qdrant'a gönder
        load_and_ingest_pdf(
            pdf_path=file_path,
            collection_name=f"{user_id}_{character_name}",
            metadata={
                "user": user_id,
                "character": character_name,
                "source": "user_upload"
            }
        )

        # PDF'teki metni oku
        loader = PyPDFLoader(str(file_path))
        pages = loader.load()
        full_text = "\n".join([page.page_content for page in pages])

        # Karakter bilgilerini çıkar
        raw_info = character_info_extraction_chain.invoke({"context": full_text})

        # "text" alanındaki JSON string'ini parse et
        if isinstance(raw_info, dict) and "text" in raw_info:
            text_content = raw_info["text"]

            # İçeriden sadece JSON kısmını ayıkla
            match = re.search(r"\{.*?\}", text_content, re.DOTALL)
            info_result = json.loads(match.group(0)) if match else {}
        else:
            info_result = {}

        return {
            "status": "ok",
            "message": "PDF başarıyla yüklendi ve analiz edildi.",
            "character_info": info_result
        }

    except Exception as e:
        return {"status": "error", "message": str(e)}
