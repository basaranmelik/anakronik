from fastapi import APIRouter
from pydantic import BaseModel
from typing import List, Tuple

# LangChain message types for history conversion
from langchain_core.messages import HumanMessage, AIMessage, BaseMessage

# Your existing service function
from services.qa_service import answer_question

router = APIRouter()

# 1. Define a Pydantic model for the request body
class ChatRequest(BaseModel):
    user_id: int
    historical_figure_id:int  
    historical_figure_name:str
    question: str
    history: List[Tuple[str, str]] = []

# 2. Update the endpoint to use the Pydantic model
@router.post("/ask")
def ask_question_endpoint(request: ChatRequest):
    """
    Handles a chat request with conversation history.
    """
    # 3. Convert the simple history list into LangChain's message objects
    chat_history_messages: List[BaseMessage] = []
    for role, content in request.history:
        if role == "human":
            chat_history_messages.append(HumanMessage(content=content))
        elif role == "ai":
            chat_history_messages.append(AIMessage(content=content))

    # 4. Call the service function with all required parameters
    response = answer_question(
        user_id=request.user_id,
        historical_figure_id=request.historical_figure_id,
        historical_figure_name=request.historical_figure_name,
        question=request.question,
        chat_history=chat_history_messages
    )

    return {"answer": response} 