# services/router_service.py

import logging
from qdrant_client import QdrantClient
from langchain_community.vectorstores import Qdrant
from config.qdrant_client import client, EMBEDDING_MODEL

logger = logging.getLogger(__name__)

SIMILARITY_THRESHOLD = 0.30

def determine_datasource(user_name: str, character_name: str, question: str) -> str:
    """
    Determines the appropriate data source ('vectorstore' or 'websearch') by checking
    for relevant documents in the specified Qdrant collection.
    """
    collection_name = f"{user_name}_{character_name}"
    logger.info(f"Checking for collection '{collection_name}' to route question.")

    try:
        vector_store = Qdrant(
            client=client,
            collection_name=collection_name,
            embeddings=EMBEDDING_MODEL,
        )

        results_with_scores = vector_store.similarity_search_with_score(
            query=question, k=1
        )

        if results_with_scores:
            top_score = results_with_scores[0][1]
            logger.info(f"Highest similarity score in '{collection_name}' is {top_score:.4f}")
            if top_score > SIMILARITY_THRESHOLD:
                logger.info(f"Score exceeds threshold ({SIMILARITY_THRESHOLD}). Routing to vectorstore.")
                return "vectorstore"

        logger.info(f"Score does not exceed threshold. Routing to websearch.")
        return "websearch"

    except Exception as e:
      
        logger.error(f"Error checking vector store for collection '{collection_name}': {e}")
        logger.warning("Defaulting to websearch due to an error.")
        return "websearch"