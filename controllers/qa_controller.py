from fastapi import APIRouter
from pydantic import BaseModel
from typing import List

# LangChain message types for history conversion
from langchain_core.messages import HumanMessage, AIMessage, BaseMessage

# Your existing service function
from services.qa_service import answer_question

router = APIRouter()

class ChatMessage(BaseModel):
    role: str
    content: str

class ChatRequest(BaseModel):
    user_id: int
    historical_figure_id: int
    historical_figure_name: str
    question: str
    history: List[ChatMessage] = []

@router.post("/ask")
def ask_question_endpoint(request: ChatRequest):
    """
    Handles a chat request with conversation history.
    """
    chat_history_messages: List[BaseMessage] = []
    for message in request.history:
        role = message.role.lower()
        if "user" in role:
            chat_history_messages.append(HumanMessage(content=message.content))
        elif "figure" in role or "ai" in role:
            chat_history_messages.append(AIMessage(content=message.content))

    response = answer_question(
        user_id=request.user_id,
        historical_figure_id=request.historical_figure_id,
        historical_figure_name=request.historical_figure_name,
        question=request.question,
        chat_history=chat_history_messages
    )

    return {"answer": response}