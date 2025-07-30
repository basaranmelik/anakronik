import React, { useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import '../styles/theme.css'; // Ortak tema stilleri
import './HomePage.css'; // Sayfaya özel stiller

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
        <h1>Anakronik Projesine Hoş Geldiniz</h1>
        <p>Geçmişin haritalarını modern teknolojiyle keşfedin.</p>
      </header>

      <div className="content-wrapper">
        <section className="about-section">
          <h2>Proje Hakkında</h2>
          <p>
            Bu proje, tarihi haritaları interaktif bir şekilde görüntülemenizi ve modern haritalarla karşılaştırmanızı sağlar. 
            Kullanıcılar, belirli dönemlere ait figürleri ve olayları harita üzerinde inceleyebilir, kendi figürlerini ekleyebilir ve diğer kullanıcılarla sohbet edebilirler.
          </p>
        </section>

        <section className="features-section">
          <h2>Özellikler</h2>
          <ul>
            <li>İnteraktif ve dinamik tarihi harita görünümü</li>
            <li>Tarihi figürler oluşturma ve haritaya ekleme</li>
            <li>Gerçek zamanlı sohbet özelliği</li>
            <li>Güvenli kullanıcı girişi ve korumalı sayfalar</li>
          </ul>
        </section>

        <section className="how-to-use-section">
          <h2>Nasıl Kullanılır?</h2>
          <ol>
            <li><strong>Kayıt Olun:</strong> Bir hesap oluşturun.</li>
            <li><strong>Giriş Yapın:</strong> Hesabınızla giriş yapın.</li>
            <li><strong>Keşfedin:</strong> Haritayı ve figürleri inceleyin.</li>
            <li><strong>Ekleyin:</strong> Kendi figürlerinizi oluşturun.</li>
          </ol>
        </section>
      </div>
    </div>
  );
};

export default HomePage;
