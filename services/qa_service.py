# services/qa_service.py

import logging
from agents.rag_agent import get_rag_agent
from agents.websearch_agent import get_websearch_agent
from agents.router_agent import determine_datasource 

logger = logging.getLogger(__name__)

def answer_question(user_name: str, character_name: str, question: str) -> str:
    logger.info(f"Answering question for user='{user_name}', character='{character_name}'")
    logger.info(f"Received question: {question}")
    
    # 1. Use the new router to determine the datasource
    datasource = determine_datasource(user_name, character_name, question)
    logger.info(f"Determined datasource: {datasource}")

    # 2. Route to the appropriate agent based on the decision
    if datasource == "vectorstore":
        collection_name = f"{user_name}_{character_name}"
        logger.info(f"Using RAG agent with collection: {collection_name}")
        rag_agent = get_rag_agent(collection_name, character_name)
        answer = rag_agent.invoke({"question": question})
        logger.info(f"RAG agent answer obtained")
    
    else:  # "websearch"
        logger.info(f"Using websearch agent for character: {character_name}")
        websearch_agent = get_websearch_agent(character_name)
        # Make sure the websearch agent receives the correct input dictionary
        answer_content = websearch_agent.invoke({"question": question})
        # The websearch agent returns a content string from the AIMessage
        answer = answer_content.content if hasattr(answer_content, 'content') else str(answer_content)
        logger.info(f"Websearch agent answer obtained")

    logger.info("Returning final answer")
    return answer