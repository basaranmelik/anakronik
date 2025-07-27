from fastapi import APIRouter,Query
from pydantic import BaseModel
from services.qa_service import answer_question

router = APIRouter()


@router.post("/ask")
def ask_question(
    user_name:str=Query(...),
    character_name:str=Query(...),
    question:str=Query(...)
):
    response = answer_question(user_name, character_name, question)
    return {"answer": response}
