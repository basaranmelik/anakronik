from fastapi import APIRouter, UploadFile, Form, File
from services.upload_service import handle_upload

router = APIRouter()

@router.post("/upload")
async def upload_pdf(
    user_id: int = Form(...),
    historical_figure_id: int = Form(...),
    historical_figure_name: str = Form(...),
    file: UploadFile = File(...)
):
    """
    Yeni bir PDF'i yenisiyle değiştirmeyi yöneten endpoint.
    """
    return await handle_upload(user_id, historical_figure_id,historical_figure_name, file)
