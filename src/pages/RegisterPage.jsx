import React, { useState, useContext } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import logo from '../assets/risk-map.svg';
import './AuthPage.css';

function RegisterPage() {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const { register } = useContext(AuthContext);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const success = await register(fullName, email, password);
    if (success) {
      navigate('/login');
    } else {
      setError('Kayıt başarısız. Lütfen bilgilerinizi kontrol edin.');
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <img src={logo} alt="Anakronik Logo" className="auth-logo" />
        <h1>Yeni Bir Hesap Oluşturun</h1>
        <p>Topluluğumuza katılın ve geçmişi keşfetmeye başlayın.</p>
        <form onSubmit={handleSubmit} className="auth-form">
          <input type="text" value={fullName} onChange={(e) => setFullName(e.target.value)} placeholder="Tam Adınız" required />
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Email" required />
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Şifre" required />
          <button type="submit">Kayıt Ol</button>
        </form>
        {error && <p className="error">{error}</p>}
        <p>Zaten bir hesabınız var mı? <Link to="/login" className="auth-link">Giriş Yapın</Link></p>
      </div>
    </div>
  );
}

export default RegisterPage;