import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import './AuthPage.css';

function ForgotPasswordPage() {
    const [email, setEmail] = useState('');
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage('');
        setError('');
        try {
            const response = await apiClient.post('/auth/forgot-password', { email });
            setMessage(response.data);
        } catch (err) {
            setError('Bir hata oluştu. Lütfen tekrar deneyin.');
        }
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                <h2>Şifre Sıfırlama</h2>
                <p>Hesabınıza ait e-posta adresini girin. Size şifrenizi sıfırlamanız için bir link göndereceğiz.</p>

                {!message ? (
                    <form onSubmit={handleSubmit} className="auth-form">
                        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Email" required />
                        <button type="submit">Sıfırlama Linki Gönder</button>
                    </form>
                ) : (
                    <p className="success-message">{message}</p>
                )}

                {error && <p className="error">{error}</p>}
                <p><Link to="/login" className="auth-link">Giriş Sayfasına Dön</Link></p>
            </div>
        </div>
    );
}

export default ForgotPasswordPage;