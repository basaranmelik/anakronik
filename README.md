# ANAKRONİK: Tarihi Kişiliklerle Etkileşimli Sohbet Deneyimi

**ANAKRONİK**, özellikle **ortaokul ve lise öğrencilerinin** tarihi kişilikleri daha eğlenceli ve etkileşimli bir şekilde tanımalarını amaçlayan bir **RAG (Retrieval-Augmented Generation)** projesidir.

Bu proje sayesinde kullanıcılar, geçmişte yaşamış önemli tarihi karakterlerle sohbet ederek onların hayatları, düşünceleri ve dönemleri hakkında bilgi edinebilirler.

---

## Projenin Amacı

Geleneksel öğrenme yöntemlerinin ötesine geçerek, kullanıcıların **sohbet tabanlı yapay zekâ karakterlerle** öğrenme sürecine aktif katılımını hedefliyoruz. Böylece tarih dersleri daha ilgi çekici ve kalıcı hale geliyor.

---

## Proje Mimarisi

Proje üç temel bileşenden oluşmaktadır ve tüm bileşenler **Docker Compose** ile konteynerleştirilmiştir:

### 1. RAG + Agentic AI Katmanı (LangChain + Gemini 2.5 + Qdrant)

- **LangChain** ile karakter tabanlı RAG yapısı kurulmuştur.
- **Gemini 2.5 Pro** modeli ile karakterlere özel cevap üretimi sağlanmaktadır.
- **Qdrant Vector Database** kullanılarak karakterlerin bilgi tabanı vektörleştirilmiştir.
- Kullanıcılar karakterlerle kendi özel oturumlarında sohbet edebilir.

### 2. Backend (Spring Boot)

- Karakter ve kullanıcı yönetimi (kayıt, giriş, karakter ekleme,sohbet).
- Admin kullanıcıları sistem genelinde görülebilecek karakterler tanımlayabilir.
- Kullanıcılar kendi karakterlerini oluşturabilir ve yükledikleri PDF’ler üzerinden karakter eğitimi sağlayabilir.

### 3. Frontend (React + Vite)

- Modern ve hızlı kullanıcı arayüzü
- Karakter listesi, sohbet arayüzü ve PDF yükleme bileşenleri
- Admin ve kullanıcı rolleri için ayrı panel erişimleri


---

## Özellikler

- **Tarihi karakterlerle gerçek zamanlı sohbet**
- **PDF ile karakter oluşturma desteği**
- **PDF alaka kontrolü**
- **PDF yetersiz kaldığında Web araştırması**
- **Kişisel sohbet geçmişi ve oturum izolasyonu**  
- **Kolay kurulum ve Docker Compose entegrasyonu**  
- **Admin tarafından tanımlanan global karakterler**  

---

## Uygulama Görselleri

### Karakter Seçim Ekranı

> *Kullanıcıların sistemdeki karakterler arasından seçim yapabildiği ekran.*

![Karakter Seçim Ekranı](https://github.com/user-attachments/assets/b59c9999-d5c3-40ad-b4fa-f0bae34a58df)

### Sohbet Ekranı

> *Seçilen karakterle etkileşimli sohbet yapılabilen ekran.*

![Sohbet Ekranı](https://github.com/user-attachments/assets/afc5e7b1-99cd-42e3-93ec-8b1f9c50441d)

---

## Projede Emeği Geçenler

| İsim               | Rol                                                     |
|--------------------|---------------------------------------------------------|
| **Bedirhan Çelik** | RAG & AI                                                |
| **Melik Başaran** | Backend Geliştirici (Spring Boot) + Docker Entegrsyonu  |
| **Gürkan Kılıç** | Frontend Geliştirici (React)                            |

> Bu proje, gençlerin öğrenme süreçlerine yapay zekâyı entegre ederek eğlenceli ve kalıcı bir deneyim sunmayı hedefleyen bir eğitim teknolojisi girişimidir.

---

> *"Tarihi öğrenmenin en keyifli yolu: Onlarla konuşmak!"*
