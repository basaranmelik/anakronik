from langchain.chains import LLMChain
from langchain_core.prompts import PromptTemplate
from config.llm_config import LLM_MODEL
from enums.world_regions_enum import WorldRegion

REGIONS = [r.value for r in WorldRegion]

template = """
The following text is about a historical figure.
Extract the following information:

- Birth date (year or full date),
- Death date (if available),
- Region of birth, using ONLY one of the following predefined regions:
{regions_list}

ONLY return the region name from the list above — do NOT return country names or cities.

Provide the response in this exact JSON format:
{{
  "birth_date": "...",
  "death_date": "...",
  "region": "..."  # Must be exactly one of: {regions_list}
}}

Text:
{context}
"""

prompt = PromptTemplate(
    template=template,
    input_variables=["context"],
    partial_variables={"regions_list": ", ".join(REGIONS)}
)

character_info_extraction_chain = LLMChain(llm=LLM_MODEL, prompt=prompt)
