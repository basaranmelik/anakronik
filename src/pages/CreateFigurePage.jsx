// src/pages/CreateFigurePage.jsx
import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import './CreateFigurePage.css';

function CreateFigurePage() {
    const [name, setName] = useState('');
    const [bio, setBio] = useState('');
    const [file, setFile] = useState(null);
    const [error, setError] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!file || !name) {
            setError('İsim ve dosya alanları zorunludur.');
            return;
        }
        setIsSubmitting(true);
        setError('');

        const formData = new FormData();
        // Sadece 'name' ve 'bio' içeren JSON objesi
        const figureData = { name, bio };

        formData.append('figureData', JSON.stringify(figureData));
        formData.append('file', file);

        try {
            await apiClient.post('/historical-figures', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });
            // Başarılı oluşturma sonrası sohbet listesine veya haritaya yönlendirebiliriz.
            // Şimdilik haritaya yönlendirelim.
            navigate('/map');
        } catch (err) {
            console.error("Figür oluşturma hatası:", err);
            setError('Figür oluşturulurken bir hata oluştu. Lütfen tekrar deneyin.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="create-figure-container">
            <form className="create-figure-form" onSubmit={handleSubmit}>
                <Link to="/map" className="back-link">← Haritaya Geri Dön</Link>
                <h2>Yeni Tarihi Figür Ekle</h2>

                <label htmlFor="name">İsim:</label>
                <input id="name" type="text" value={name} onChange={(e) => setName(e.target.value)} required />

                <label htmlFor="bio">Biyografi (Karakter için kısa bir tanım):</label>
                <textarea id="bio" value={bio} onChange={(e) => setBio(e.target.value)} />

                <label htmlFor="file">Bilgi Dokümanı:</label>
                <input id="file" type="file" onChange={(e) => setFile(e.target.files[0])} required />

                {error && <p className="error-message">{error}</p>}

                <button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? 'Oluşturuluyor...' : 'Oluştur'}
                </button>
            </form>
        </div>
    );
}

export default CreateFigurePage;