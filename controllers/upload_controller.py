from fastapi import APIRouter, UploadFile, Form, File
from services.upload_service import handle_upload,handle_update

router = APIRouter()

@router.post("/upload/")
async def upload_pdf(
    user_id: int = Form(...),
    historical_figure_id: int = Form(...),
    historical_figure_name: str = Form(...),
    file: UploadFile = File(...)
):
    return await handle_upload(user_id, historical_figure_id,historical_figure_name, file)

@router.put("/update/")
async def update_pdf(
    user_id: int = Form(...),
    historical_figure_id: int = Form(...),
    historical_figure_name: str = Form(...),
    file: UploadFile = File(...)
):
    """
    Mevcut bir PDF'i yenisiyle değiştirmeyi yönetir.
    Bu endpoint, yeni dosyayı işlemeden önce eski vektörleri ve dosyayı siler.
    """
    return await handle_update(user_id, historical_figure_id, historical_figure_name, file)