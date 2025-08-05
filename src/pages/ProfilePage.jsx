import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import './ProfilePage.css';

function ProfilePage() {
    const [user, setUser] = useState(null);
    const [figures, setFigures] = useState([]);
    const [fullName, setFullName] = useState('');
    const [message, setMessage] = useState('');
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmNewPassword, setConfirmNewPassword] = useState('');
    const [error, setError] = useState('');
    const [uploadTargetId, setUploadTargetId] = useState(null);
    const navigate = useNavigate();
    const fileInputRef = useRef(null);



    useEffect(() => {
        const fetchData = async () => {
            try {
                const userResponse = await apiClient.get('/users/profile');
                const currentUser = userResponse.data;
                setUser(currentUser);
                setFullName(currentUser.fullName);

                const figuresResponse = await apiClient.get('/historical-figures');
                const allFigures = figuresResponse.data.content || [];

                const myFigures = allFigures.filter(figure =>
                    figure.createdByUsername === currentUser.email
                );

                setFigures(myFigures);

            } catch (error) {
                console.error("Veri alınırken hata oluştu:", error);
            }
        };
        fetchData();
    }, []);

    useEffect(() => {
        if (message) {
            const timer = setTimeout(() => {
                setMessage('');
            }, 3000);
            return () => clearTimeout(timer);
        }
    }, [message]);

    useEffect(() => {
        if (error) {
            const timer = setTimeout(() => {
                setError('');
            }, 5000);
            return () => clearTimeout(timer);
        }
    }, [error]);

    const handleNameUpdate = async (e) => {
        e.preventDefault();
        try {
            const response = await apiClient.put('/users/profile', { fullName });
            setUser(response.data);
            setMessage('İsim başarıyla güncellendi.');
        } catch (error) {
            console.error("İsim güncellenirken hata:", error);
            setMessage('Bir hata oluştu.');
        }
    };

    const handlePasswordChange = async (e) => {
        e.preventDefault();
        setMessage('');
        setError('');

        if (newPassword !== confirmNewPassword) {
            setError('Yeni şifreler eşleşmiyor.');
            return;
        }

        if (newPassword.length < 8) {
            setError('Yeni şifre en az 8 karakter olmalıdır.');
            return;
        }

        try {
            const response = await apiClient.post('/auth/change-password', {
                oldPassword: currentPassword,
                newPassword: newPassword
            });
            setMessage(response.data);
            setCurrentPassword('');
            setNewPassword('');
            setConfirmNewPassword('');
        } catch (err) {
            console.error("Şifre değiştirilirken hata:", err);
            setError(err.response?.data?.message || 'Şifre değiştirilirken bir hata oluştu. Mevcut şifrenizi doğru girdiğinizden emin olun.');
        }
    };

    const handleAddDocumentClick = (figureId) => {
        setUploadTargetId(figureId);
        fileInputRef.current.click();
    };

    const handleFileSelected = async (event) => {
        const file = event.target.files[0];
        if (!file || !uploadTargetId) return;

        const formData = new FormData();
        formData.append('file', file);
        formData.append('docName', file.name);

        try {
            await apiClient.post(`/historical-figures/${uploadTargetId}/add-document`, formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            alert(`'${file.name}' dokümanı başarıyla eklendi.`);
        } catch (error) {
            console.error("Doküman eklenirken hata oluştu:", error);
            alert("Doküman eklenirken bir hata oluştu.");
        }
        event.target.value = null;
        setUploadTargetId(null);
    };

    if (!user) {
        return <div>Yükleniyor...</div>;
    }

    const handleDeleteFigure = async (figureId, figureName) => {
        if (window.confirm(`'${figureName}' karakterini silmek istediğinize emin misiniz?`)) {
            try {
                await apiClient.delete(`/historical-figures/${figureId}`);

                setFigures(figures.filter(f => f.id !== figureId));
            } catch (error) {
                console.error("Figür silinirken hata oluştu:", error);
                alert("Figür silinirken bir hata oluştu.");
            }
        }
    };

    return (
        <div className="profile-page-container">
            <input type="file" ref={fileInputRef} style={{ display: 'none' }} onChange={handleFileSelected} />

            <div className="profile-card">
                <Link to="/map" className="back-link">← Haritaya Geri Dön</Link>
                <h2>Profilim</h2>

                <div className="profile-info">
                    <strong>Email:</strong> {user.email}
                </div>

                <form onSubmit={handleNameUpdate} className="profile-form">
                    <label htmlFor="fullName">Tam İsim:</label>
                    <input
                        id="fullName"
                        type="text"
                        value={fullName}
                        onChange={(e) => setFullName(e.target.value)}
                    />
                    <button type="submit">İsmi Güncelle</button>
                </form>

                <hr className="form-separator" />

                <form onSubmit={handlePasswordChange} className="profile-form">
                    <h4>Şifre Değiştir</h4>
                    <label htmlFor="currentPassword">Mevcut Şifre:</label>
                    <input id="currentPassword" type="password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} required />

                    <label htmlFor="newPassword">Yeni Şifre:</label>
                    <input id="newPassword" type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required />

                    <label htmlFor="confirmNewPassword">Yeni Şifre (Tekrar):</label>
                    <input id="confirmNewPassword" type="password" value={confirmNewPassword} onChange={(e) => setConfirmNewPassword(e.target.value)} required />

                    <button type="submit">Şifreyi Değiştir</button>
                </form>

                {message && <p className="success-message">{message}</p>}
                {error && <p className="error-message">{error}</p>}
            </div>

            <div className="figures-list-card">
                <h3>Oluşturduğum Figürler ({figures.length})</h3>
                <ul className="figures-list">
                    {figures.length > 0 ? (
                        figures.map(fig => (

                            <li key={fig.id}>
                                <div className="figure-info" onClick={() => navigate(`/chat/${fig.id}`)}>
                                    {fig.imageUrl ? (
                                        <img src={`http://localhost:8080${fig.imageUrl}`} alt={fig.name} className="figure-list-image" />
                                    ) : (
                                        <div className="figure-list-placeholder"></div>
                                    )}
                                    <span>{fig.name}</span>
                                </div>
                                {user && fig.createdByUsername === user.email && (
                                    <div className="figure-actions">
                                        <button className="action-button add-doc-button" onClick={() => handleAddDocumentClick(fig.id)}>Döküman Ekle</button>
                                        <button className="action-button delete-button" onClick={() => handleDeleteFigure(fig.id, fig.name)}>Sil</button>
                                    </div>
                                )}
                            </li>
                        ))
                    ) : (
                        <p>Henüz hiç figür oluşturmadınız.</p>
                    )}
                </ul>
            </div>
        </div>
    );
}

export default ProfilePage;