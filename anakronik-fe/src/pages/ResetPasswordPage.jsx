import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import './AuthPage.css';

function ResetPasswordPage() {
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');
    const [searchParams] = useSearchParams();
    const [token, setToken] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const tokenFromUrl = searchParams.get('token');
        if (tokenFromUrl) {
            setToken(tokenFromUrl);
        } else {
            setError("Geçersiz veya eksik şifre sıfırlama linki.");
        }
    }, [searchParams]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (password !== confirmPassword) {
            setError('Şifreler eşleşmiyor.');
            return;
        }
        setError('');
        setMessage('');

        try {
            const response = await apiClient.post('/auth/reset-password', { token, newPassword: password });
            setMessage(response.data + " Giriş sayfasına yönlendiriliyorsunuz...");
            setTimeout(() => navigate('/login'), 3000);
        } catch (err) {
            setError('Şifre sıfırlanırken bir hata oluştu. Linkin süresi dolmuş olabilir.');
        }
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                <h2>Yeni Şifre Belirle</h2>

                {token && !message && (
                    <form onSubmit={handleSubmit} className="auth-form">
                        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Yeni Şifre" required />
                        <input type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} placeholder="Yeni Şifre (Tekrar)" required />
                        <button type="submit">Şifreyi Güncelle</button>
                    </form>
                )}

                {message && <p className="success-message">{message}</p>}
                {error && <p className="error">{error}</p>}
                {message && <p><Link to="/login" className="auth-link">Şimdi Giriş Yap</Link></p>}
            </div>
        </div>
    );
}

export default ResetPasswordPage;