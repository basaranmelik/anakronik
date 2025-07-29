import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const success = await login(email, password);
    if (success) {
      navigate('/map'); // Başarılı giriş sonrası harita sayfasına yönlendir
    } else {
      setError('Email veya şifre hatalı.');
    }
  };

  return (
    <div className="form-container">
      <h2>Giriş Yap</h2>
      <form onSubmit={handleSubmit}>
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Email" required />
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Şifre" required />
        <button type="submit">Giriş Yap</button>
      </form>
      {error && <p className="error">{error}</p>}
      <p>Hesabın yok mu? <Link to="/register">Kayıt Ol</Link></p>
    </div>
  );
}

export default LoginPage;