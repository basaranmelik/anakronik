import React, { useState, useContext } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import logo from '../assets/anakronik_logo.png';
import './AuthPage.css';

function RegisterPage() {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const { register } = useContext(AuthContext);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');

    try {
      const response = await register(fullName, email, password);
      setMessage(response.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Kayıt başarısız. Lütfen bilgilerinizi kontrol edin.');
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <img src={logo} alt="Anakronik Logo" className="auth-logo" />
        <h1>Yeni Bir Hesap Oluşturun</h1>

        {!message ? (
          <>
            <p>Kendi yapay zeka karakterlerinizi yaratmak ve tarihle sohbet etmek için topluluğumuza katılın.</p>
            <form onSubmit={handleSubmit} className="auth-form">
              <input type="text" value={fullName} onChange={(e) => setFullName(e.target.value)} placeholder="Tam Adınız" required />
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Email" required />
              <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Şifre (en az 8 karakter)" required />
              <button type="submit">Hesap Oluştur</button>
            </form>
          </>
        ) : (
          <p className="success-message">{message}</p>
        )}

        {error && <p className="error">{error}</p>}
        <p>Zaten bir hesabınız var mı? <Link to="/login" className="auth-link">Giriş Yapın</Link></p>
      </div>
    </div>
  );
}

export default RegisterPage;