import React, { useState, useContext, useEffect } from 'react';
import { useNavigate, Link, useSearchParams } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import logo from '../assets/anakronik_logo.png';
import './AuthPage.css';

function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const { login } = useContext(AuthContext);

  const [searchParams] = useSearchParams();
  const [notification, setNotification] = useState('');

  useEffect(() => {
    if (searchParams.get('verified') === 'true') {
      setNotification('Hesabınız başarıyla doğrulandı. Şimdi giriş yapabilirsiniz.');
    }
  }, [searchParams]);


  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const success = await login(email, password);
    if (success) {
      navigate('/');
    } else {
      setError('Email veya şifre hatalı.');
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <img src={logo} alt="Anakronik Logo" className="auth-logo" />
        <h1>Geçmişe Yolculuk Başlasın</h1>
        <p>Hesabınıza giriş yaparak tarihi figürlerle sohbete kaldığınız yerden devam edin.</p>

        {notification && <p className="auth-notification">{notification}</p>}

        <form onSubmit={handleSubmit} className="auth-form">
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Email" required />
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Şifre" required />
          <button type="submit">Giriş Yap</button>
        </form>
        {error && <p className="error">{error}</p>}
        <p className="forgot-password-link">
          <Link to="/forgot-password">Şifremi Unuttum</Link>
        </p>

        <p>Henüz bir hesabın yok mu? <Link to="/register" className="auth-link">Hemen Kayıt Ol</Link></p>
      </div>
    </div>
  );
}

export default LoginPage;