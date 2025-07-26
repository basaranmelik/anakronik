from langchain_google_genai import ChatGoogleGenerativeAI
from dotenv import load_dotenv
import os
load_dotenv()
llm = ChatGoogleGenerativeAI(model="gemini-2.5-pro")

prompt = "Türkiyenin başkenti neresidir"
result = llm.invoke(prompt)
print(result.content)