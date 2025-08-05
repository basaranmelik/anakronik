import React, { useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import '../styles/theme.css';
import './HomePage.css';

const HomePage = () => {
  const { isAuthenticated, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="page-container">
      <nav className="navbar">
        <div className="navbar-brand">
          <Link to="/">Anakronik</Link>
        </div>
        <div className="navbar-links">
          {isAuthenticated ? (
            <>
              <Link to="/map">Harita</Link>
              <Link to="/profile">Profil</Link>
              <button onClick={handleLogout} className="nav-button">Çıkış Yap</button>
            </>
          ) : (
            <>
              <Link to="/login">Giriş Yap</Link>
              <Link to="/register">Kayıt Ol</Link>
            </>
          )}
        </div>
      </nav>

      <header className="hero-section">
        <h1>Tarihle Sohbet Edin</h1>
        <p>Yapay zekanın gücüyle, tarihin en büyük zihinleriyle tanışın, öğrenin ve etkileşime geçin.</p>
      </header>

      <div className="content-wrapper">
        <section className="about-section">
          <h2>Anakronik Nedir?</h2>
          <p>
            Anakronik, tarihi figürleri yapay zeka aracılığıyla hayata geçiren bir platformdur. İstediğiniz bir karakter hakkında bilgi dokümanları yükleyerek, o kişinin bilgi ve üslubuna sahip dijital bir elçisini yaratabilirsiniz. Bu elçilerle sohbet edebilir, onlara sorular sorabilir ve geçmişe dair eşsiz bir bakış açısı kazanabilirsiniz.
          </p>
        </section>

        <section className="features-section">
          <h2>Özellikler</h2>
          <ul>
            <li><strong>Kendi Yapay Zekanızı Yaratın:</strong> Dilediğiniz tarihi figür hakkında bir doküman yükleyin ve onun dijital kişiliğini hayata geçirin.</li>
            <li><strong>İnteraktif Dünya Haritası:</strong> Oluşturulan tüm karakterlerin hangi coğrafyalarda yaşadığını keşfedin.</li>
            <li><strong>Geçmişle Diyalog Kurun:</strong> Yarattığınız veya diğer kullanıcıların yarattığı yapay zeka kişilikleriyle derin sohbetlere dalın.</li>
            <li><strong>Kişisel Kütüphaneniz:</strong> Oluşturduğunuz tüm figürler, dokümanlar ve sohbet geçmişleriniz profil sayfanızda güvende.</li>
          </ul>
        </section>

        <section className="how-to-use-section">
          <h2>Nasıl Başlanır?</h2>
          <ol>
            <li><strong>Hesap Oluşturun:</strong> Hızlıca kaydolarak topluluğumuza katılın.</li>
            <li><strong>Figür Yaratın:</strong> İlham aldığınız bir karakter seçin, onun hakkında bir metin yükleyin ve yapay zekanın onu hayata geçirmesini izleyin.</li>
            <li><strong>Sohbete Başlayın:</strong> Haritadan veya sohbet sayfasından bir karakter seçerek geçmişe yolculuğunuza başlayın.</li>
          </ol>
        </section>
      </div>
    </div>
  );
};

export default HomePage;