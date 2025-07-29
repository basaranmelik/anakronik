import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function RegisterPage() {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const { register } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const success = await register(fullName, email, password);
    if (success) {
      navigate('/login'); // Başarılı kayıt sonrası login sayfasına yönlendir
    } else {
      setError('Kayıt başarısız. Lütfen bilgilerinizi kontrol edin.');
    }
  };

  return (
    <div className="form-container">
      <h2>Kayıt Ol</h2>
      <form onSubmit={handleSubmit}>
        <input type="text" value={fullName} onChange={(e) => setFullName(e.target.value)} placeholder="Tam Adınız" required />
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Email" required />
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Şifre" required />
        <button type="submit">Kayıt Ol</button>
      </form>
      {error && <p className="error">{error}</p>}
      <p>Zaten bir hesabın var mı? <Link to="/login">Giriş Yap</Link></p>
    </div>
  );
}

export default RegisterPage;