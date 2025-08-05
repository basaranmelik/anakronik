# Anakronik - Frontend

Bu proje, tarihi figürlerle yapay zeka destekli sohbet imkanı sunan Anakronik uygulamasının React ile geliştirilmiş frontend (kullanıcı arayüzü) kısmıdır. Kullanıcılar, interaktif bir harita üzerinde tarihi karakterleri keşfedebilir, kendi karakterlerini bilgi dokümanları yükleyerek oluşturabilir ve bu karakterlerle sohbet edebilirler.

## Proje Vizyonu

Anakronik, tarih öğrenimini statik bir bilgi aktarımından çıkarıp, dinamik ve kişisel bir diyalog haline getirmeyi hedefler. Yapay zekanın gücüyle, tarihin en büyük zihinleriyle tanışma, onları kendi bilgi kaynaklarıyla eğitme ve onlarla etkileşime geçme imkanı sunarak kullanıcılar için unutulmaz bir keşif deneyimi yaratır.

## Temel Özellikler

- **Güvenli Kimlik Doğrulama:** E-posta doğrulamalı kayıt, giriş, şifre sıfırlama ve korumalı sayfa (rota) altyapısı.
- **İnteraktif SVG Harita:** Bölgelere ayrılmış, üzerine gelince ve tıklanınca etkileşim sunan, her bölgedeki karakter sayısını Roman rakamıyla gösteren dinamik dünya haritası.
- **Dinamik Sohbet Arayüzü:**
    - Kalıcı sohbet geçmişini görüntüleme.
    - Gemini/ChatGPT benzeri, tam sayfa ve modern sohbet arayüzü.
    - Sohbet edilen karaktere ait detayları içeren bilgi kartı.
    - Karakterler arasında kolayca geçiş yapma imkanı.
    - Sohbet geçmişini sıfırlama seçeneği.
- **Karakter Yönetimi:**
    - Kullanıcıların kendi tarihi figürlerini (isim, resim ve bilgi dokümanı ile) oluşturması.
    - Mevcut bir karaktere yeni bilgi dokümanları eklemesi.
    - Sadece kendi oluşturduğu karakterler üzerinde tam yönetim (silme, doküman ekleme) yetkisi.
- **Kullanıcı Profili:** Kullanıcıların isim ve şifre gibi kişisel bilgilerini güncelleyebildiği ve kendi oluşturdukları tüm karakterleri listeleyebildiği bir sayfa.
- **Admin Paneli:**
    - Sadece `ADMIN` rolüne sahip kullanıcıların erişebildiği özel yönetim alanı.
    - Sistemdeki tüm kullanıcıları ve tüm tarihi figürleri listeleme ve yönetme (silme, düzenleme vb.) imkanı.

## Kullanılan Teknolojiler ve Kütüphaneler

- **React:** Kullanıcı arayüzünü oluşturmak için kullanılan ana kütüphane.
- **Vite:** Hızlı ve modern bir geliştirme ortamı ve proje derleyicisi.
- **React Router DOM:** Sayfalar arası geçişleri (routing) yönetmek için.
- **Axios:** Backend API'si ile iletişim kurmak için kullanılan HTTP istemcisi.
- **SVG:** İnteraktif dünya haritasını oluşturmak ve bölgeleri yönetmek için (`vite-plugin-svgr` ile birlikte).
- **CSS:** Uygulamanın bütününde tutarlı ve şık bir tema oluşturmak için (Flexbox, Grid vb.).

## Sayfa ve Component Yapısı

- **/pages:** Ana sayfa görünümlerini içerir.
    - `HomePage.jsx`: Karşılama sayfası.
    - `LoginPage.jsx` / `RegisterPage.jsx`: Kimlik doğrulama formları.
    - `ForgotPasswordPage.jsx` / `ResetPasswordPage.jsx`: Şifre sıfırlama akışı.
    - `MapPage.jsx`: İnteraktif haritanın bulunduğu ana ekran.
    - `ChatPage.jsx`: Sohbet arayüzü.
    - `ProfilePage.jsx`: Kullanıcı profili ve figür listesi.
    - `AdminPage.jsx` / `AdminFiguresPage.jsx`: Yönetici paneli sayfaları.
    - `ErrorPage.jsx`: Hata ve bulunamayan sayfalar.
- **/components:** Birden fazla sayfada kullanılabilen küçük ve yeniden kullanılabilir UI parçalarını içerir.
    - `ProtectedRoute.jsx`: Sadece giriş yapmış kullanıcıların ve/veya belirli rollere sahip olanların erişebileceği rotaları korur.
- **/context:** Uygulama genelinde state yönetimi için kullanılır.
    - `AuthContext.jsx`: Kullanıcının giriş durumu, rolü ve kimlik bilgilerini backend'den alarak tutar.
- **/api:** Backend ile iletişimi yönetir.
    - `axiosConfig.js`: `accessToken`'ı her isteğe otomatik ekleyen ve ana URL'yi tanımlayan merkezi Axios yapılandırması.
- **/utils:** Yardımcı fonksiyonları ve veri haritalarını içerir.
    - `regionMapping.js`: SVG ID'leri ile Türkçe bölge isimleri arasındaki çeviriyi yapar.
- **/assets:** SVG, resim gibi statik dosyaları içerir.