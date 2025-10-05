import React from 'react';
import { Link } from 'react-router-dom';
import './ErrorPage.css';

function ErrorPage() {
    return (
        <div className="error-page-container">
            <div className="error-card">
                <h1>Oops!</h1>
                <h2>Aradığınız Sayfa Bulunamadı</h2>
                <p>Ulaşmaya çalıştığınız sayfa mevcut değil veya taşınmış olabilir.</p>
                <Link to="/map" className="error-home-button">
                    Ana Haritaya Geri Dön
                </Link>
            </div>
        </div>
    );
}

export default ErrorPage;