import logging
from typing import List
from langchain_core.messages import BaseMessage
from agents.rag_agent import get_rag_agent
from agents.websearch_agent import get_websearch_agent
from agents.router_agent import determine_route 

logger = logging.getLogger(__name__)

def answer_question(user_id: int, historical_figure_id:int,historical_figure_name:str, question: str, chat_history: List[BaseMessage]) -> str:
    logger.info(f"Answering question for user='{user_id}', character='{historical_figure_id}' with history.")
    logger.info(f"Received question: {question}")
    
    route = determine_route(user_id, historical_figure_id,historical_figure_name, question, chat_history)
    logger.info(f"Determined route: {route}")

    if route == "vectorstore":
        collection_name = f"{user_id}_{historical_figure_id}"
        logger.info(f"Using RAG agent with collection: {collection_name}")
        rag_agent = get_rag_agent(collection_name)
        answer = rag_agent.invoke({
            "question": question, 
            "historical_figure_id": historical_figure_id,
            "historical_figure_name":historical_figure_name, 
            "chat_history": chat_history
        })
        logger.info(f"RAG agent answer obtained")

    elif route == "websearch":
        logger.info(f"Using websearch agent for character: {historical_figure_id}")
        websearch_agent = get_websearch_agent()
        answer = websearch_agent.invoke({
            "question": question, 
            "historical_figure_id": historical_figure_id, 
            "historical_figure_name":historical_figure_name,
            "chat_history": chat_history
        })
        logger.info(f"Websearch agent answer obtained")
        
    else: 
        logger.info("Question is out of scope. Generating rejection message.")
        answer = f"I am {historical_figure_name}. I'm afraid that question is outside my realm of knowledge, and I cannot provide an answer."

    logger.info("Returning final answer")
    return answer