from fastapi import APIRouter, UploadFile, Form, File
from services.upload_service import handle_upload

router = APIRouter()

@router.post("/upload/")
async def upload_pdf(
    user_id: str = Form(...),
    character_name: str = Form(...),
    file: UploadFile = File(...)
):
    return await handle_upload(user_id, character_name, file)
