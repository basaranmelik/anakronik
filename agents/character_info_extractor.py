from langchain.chains import LLMChain
from langchain_core.prompts import PromptTemplate
from config.llm_config import LLM_MODEL
# Türkçe enum'u import ettiğinizi varsayıyorum
from enums.world_regions_enum import WorldRegionTR 

REGIONS = [r.value for r in WorldRegionTR]

# --- GÜNCELLENMİŞ VE GÜÇLENDİRİLMİŞ TEMPLATE ---
template = """
Aşağıdaki metin tarihi bir şahsiyet hakkındadır.
Görevin, metinden istenen bilgileri çıkarmak ve birebir istenen JSON formatında sunmaktır.

İstenen Bilgiler:
- Doğum tarihi (yıl veya tam tarih),
- Ölüm tarihi (eğer mevcutsa),
- Doğum bölgesi (SADECE şu listeden seçilmelidir: {regions_list})

Kurallar:
1. Cevabın SADECE geçerli bir JSON objesi olmalıdır.
2. 'region' değeri kesinlikle verilen listeden seçilmelidir. Başka bir bölge, ülke veya şehir adı YAZMA.

--- Tarih Formatı Kuralları ---
3. Tarihleri 'GG Ay YYYY' formatında ve Türkçe ay isimleri (Ocak, Şubat, Mart, Nisan vb.) kullanarak yazmalısın.
4. Metindeki tarih İngilizce ise (Örn: "April 30, 1777"), onu mutlaka Türkçe'ye çevirmelisin.

Örnek:
- Metinde "born on April 30, 1777" yazıyorsa, JSON'da "birth_date": "30 Nisan 1777" olmalıdır.
- Metinde "died on February 23, 1855" yazıyorsa, JSON'da "death_date": "23 Şubat 1855" olmalıdır.

JSON Formatı:
{{
  "birth_date": "...",
  "death_date": "...",
  "region": "..."
}}

Metin:
{context}
"""

prompt = PromptTemplate(
    template=template,
    input_variables=["context"],
    partial_variables={"regions_list": ", ".join(REGIONS)}
)

character_info_extraction_chain = LLMChain(llm=LLM_MODEL, prompt=prompt)