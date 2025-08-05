from pydantic import BaseModel
from typing import List
from models.ChatMessage import ChatMessage

class ChatRequest(BaseModel):
    user_id: int
    historical_figure_id: int
    historical_figure_name: str
    question: str
    history: List[ChatMessage] = []
