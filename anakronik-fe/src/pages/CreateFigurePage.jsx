import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import './CreateFigurePage.css';

function CreateFigurePage() {
    const [name, setName] = useState('');
    const [infoFile, setInfoFile] = useState(null);
    const [imageFile, setImageFile] = useState(null);
    const [error, setError] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!name || !infoFile || !imageFile) {
            setError('İsim, bilgi dokümanı ve resim dosyası alanları zorunudur.');
            return;
        }
        setIsSubmitting(true);
        setError('');

        const formData = new FormData();
        const figureData = { name };

        const figureDataBlob = new Blob([JSON.stringify(figureData)], {
            type: 'application/json'
        });
        formData.append('figureData', figureDataBlob);
        formData.append('file', infoFile);
        formData.append('image', imageFile);

        try {
            await apiClient.post('/historical-figures', formData);
            navigate('/map');
        } catch (err) {
            console.error("Figür oluşturma hatası:", err);

            const errorMessage = err.response?.data || 'Figür oluşturulurken bir hata oluştu. Lütfen tekrar deneyin.';
            setError(errorMessage);

        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                <form className="create-figure-form" onSubmit={handleSubmit}>
                    <Link to="/map" className="back-link">← Haritaya Geri Dön</Link>
                    <h2>Yeni Tarihi Figür Ekle</h2>

                    <label htmlFor="name">İsim:</label>
                    <input id="name" type="text" value={name} onChange={(e) => setName(e.target.value)} required />

                    <label htmlFor="infoFile">Bilgi Dokümanı (PDF, DOCX, vb.):</label>
                    <input id="infoFile" type="file" onChange={(e) => setInfoFile(e.target.files[0])} required />

                    <label htmlFor="imageFile">Figür Resmi (JPG, PNG, vb.):</label>
                    <input id="imageFile" type="file" accept="image/*" onChange={(e) => setImageFile(e.target.files[0])} required />

                    {error && <p className="error-message">{error}</p>}

                    <button type="submit" disabled={isSubmitting}>
                        {isSubmitting ? 'Oluşturuluyor...' : 'Oluştur'}
                    </button>
                </form>
            </div>
        </div>
    );
}

export default CreateFigurePage;
