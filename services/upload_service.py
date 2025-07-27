import shutil
import json
import re
from pathlib import Path
from utils.ingestion_loader import load_and_ingest_pdf
from langchain_community.document_loaders import PyPDFLoader
from agents.character_info_extractor import character_info_extraction_chain
from agents.character_validation_agent import character_validation_chain

async def handle_upload(user_id: str, character_name: str, file):
    try:
        temp_folder = Path(f"uploads/temp/{user_id}")
        temp_folder.mkdir(parents=True, exist_ok=True)
        temp_file_path = temp_folder / file.filename

        with open(temp_file_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)

        loader = PyPDFLoader(str(temp_file_path))
        pages = loader.load()
        full_text = "\n".join([page.page_content for page in pages])

        validation_result = character_validation_chain.invoke({
            "character_name": character_name,
            "context": full_text
        })

        if isinstance(validation_result, dict):
            raw_output = validation_result.get("text") or validation_result.get("output")
        else:
            raw_output = validation_result

        if "no" in raw_output.lower():
            return {
                "status": "error",
                "message": f"Yüklenen PDF {character_name} ile ilgili görünmüyor. Lütfen doğru kişilikle ilgili bir belge yükleyin."
            }

        user_folder = Path(f"uploads/{user_id}/{character_name}")
        user_folder.mkdir(parents=True, exist_ok=True)
        file_path = user_folder / file.filename
        shutil.move(str(temp_file_path), file_path)

        load_and_ingest_pdf(
            pdf_path=file_path,
            collection_name=f"{user_id}_{character_name}",
            metadata={
                "user": user_id,
                "character": character_name,
                "source": "user_upload"
            }
        )

        # Karakter bilgilerini çıkar
        raw_info = character_info_extraction_chain.invoke({"context": full_text})
        if isinstance(raw_info, dict) and "text" in raw_info:
            match = re.search(r"\{.*?\}", raw_info["text"], re.DOTALL)
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
