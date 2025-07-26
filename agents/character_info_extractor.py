from langchain.chains import LLMChain
from langchain_core.prompts import PromptTemplate
from langchain_google_genai import ChatGoogleGenerativeAI

llm = ChatGoogleGenerativeAI(model="gemini-2.5-pro")

template = """
The following text is about a historical figure.
Extract the following information from the text:

- Birth date (year or full date),
- Death date (if available),
- Place of birth (region, city, etc.)

Provide the information in the following JSON format:

Text:
{context}

RESPONSE MUST BE IN THIS EXACT FORMAT ONLY:
{{
  "birth_date": "...",
  "death_date": "...",
  "birth_place": "..."
}}
"""


prompt = PromptTemplate(
    template=template,
    input_variables=["context"]
)

character_info_extraction_chain = LLMChain(llm=llm, prompt=prompt)
