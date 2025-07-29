import shutil
import json
import re
import tempfile # tempfile kütüphanesini import edin
from pathlib import Path
from utils.ingestion_loader import load_and_ingest_pdf
from langchain_community.document_loaders import PyPDFLoader
from agents.character_info_extractor import character_info_extraction_chain
from agents.character_validation_agent import character_validation_chain
from services.query_service import delete_collection

async def handle_upload(user_id: int, historical_figure_id: int, historical_figure_name: str, file):
    # 'with' bloğu sayesinde bu geçici dizin ve içindekiler,
    # işlem bitince veya hata oluşunca otomatik olarak silinir.
    with tempfile.TemporaryDirectory() as temp_dir:
        try:
            temp_file_path = Path(temp_dir) / file.filename

            # 1. Dosyayı doğrudan geçici dizine kaydet
            with open(temp_file_path, "wb") as buffer:
                shutil.copyfileobj(file.file, buffer)

            # 2. Tüm işlemleri bu geçici dosya üzerinden yap
            loader = PyPDFLoader(str(temp_file_path))
            pages = loader.load()
            full_text = "\n".join([page.page_content for page in pages])

            validation_result = character_validation_chain.invoke({
                "historical_figure_name": historical_figure_name,
                "context": full_text
            })

            # ... (doğrulama mantığı aynı)
            if isinstance(validation_result, dict):
                raw_output = validation_result.get("text") or validation_result.get("output")
            else:
                raw_output = validation_result
            if "no" in raw_output.lower():
                return {
                    "status": "error",
                    "message": f"Yüklenen PDF {historical_figure_name} ile ilgili görünmüyor. Lütfen doğru kişilikle ilgili bir belge yükleyin."
                }
            # Doğrulama başarılıysa devam et...

            # 3. Vektör deposuna yükleme işlemini geçici dosya üzerinden yap
            load_and_ingest_pdf(
                pdf_path=temp_file_path, # Kalıcı yola taşıma yok
                collection_name=f"{user_id}_{historical_figure_id}",
                metadata={
                    "user": user_id,
                    "figure_id": historical_figure_id,
                    "figure_name": historical_figure_name,
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

            # İşlem başarılı. Geçici dizin ve dosya 'with' bloğu sonunda otomatik silinecek.
            return {
                "status": "ok",
                "message": "PDF başarıyla yüklendi.",
                "figure_info": info_result
            }

        except Exception as e:
            # Hata durumunda bile 'with' bloğu geçici dizini temizleyecektir.
            return {"status": "error", "message": str(e)}

async def handle_update(user_id: int, historical_figure_id: int, historical_figure_name: str, file):
    """
    Mevcut bir PDF'i güncellemeyi yönetir.
    Önce eski veriyi (vektörler) siler, ardından yenisini yükler.
    Eski dosya sistemini temizler (varsa).
    """
    try:
        collection_name = f"{user_id}_{historical_figure_id}"
        # Eski versiyondan kalmış olabilecek kalıcı klasörleri temizle
        user_folder = Path(f"uploads/{user_id}/{historical_figure_id}")

        # Önce vektörleri sil
        delete_collection(collection_name)

        # Sonra eski dosya yapısını sil (varsa)
        if user_folder.exists():
            shutil.rmtree(user_folder)

        # Yeni, temiz yükleme işlemini çağır
        upload_result = await handle_upload(user_id, historical_figure_id, historical_figure_name, file)

        if upload_result["status"] == "ok":
            upload_result["message"] = "PDF başarıyla güncellendi."

        return upload_result

    except Exception as e:
        return {"status": "error", "message": f"Güncelleme sırasında bir hata oluştu: {str(e)}"}