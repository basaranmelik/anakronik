import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import './AdminPage.css';

function AdminFiguresPage() {
    const [figures, setFigures] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        fetchFigures();
    }, []);

    const fetchFigures = () => {
        setLoading(true);

        apiClient.get('/admin/historical-figures')
            .then(response => {
                setFigures(response.data.content || []);
                setLoading(false);
            })
            .catch(err => {
                console.error("Figürler çekilirken hata:", err);
                setError("Figür listesi yüklenemedi. Yetkiniz olduğundan emin olun.");
                setLoading(false);
            });
    };

    const handleDeleteFigure = async (figureId, figureName) => {
        if (window.confirm(`'${figureName}' karakterini silmek istediğinize emin misiniz?`)) {
            try {
                await apiClient.delete(`/historical-figures/${figureId}`);
                fetchFigures();
            } catch (err) {
                alert("Figür silinirken bir hata oluştu.");
            }
        }
    };

    if (loading) return <div className="admin-page-container"><h2>Yükleniyor...</h2></div>;
    if (error) return <div className="admin-page-container"><h2>{error}</h2></div>;

    return (
        <div className="admin-page-container">
            <header className="admin-header">
                <h1>Admin Paneli</h1>
                <nav className="admin-nav">
                    <Link to="/admin/users">Kullanıcı Yönetimi</Link>
                    <Link to="/admin/figures">Figür Yönetimi</Link>
                </nav>
                <Link to="/map" className="back-link">← Haritaya Geri Dön</Link>
            </header>
            <table className="users-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>İsim</th>
                        <th>Bölge</th>
                        <th>Oluşturan</th>
                        <th>İşlemler</th>
                    </tr>
                </thead>
                <tbody>
                    {figures.map(fig => (
                        <tr key={fig.id}>
                            <td>{fig.id}</td>
                            <td>{fig.name}</td>
                            <td>{fig.region}</td>
                            <td>{fig.createdByUsername}</td>
                            <td>
                                <button className="action-button edit-button" onClick={() => navigate(`/chat/${fig.id}`)}>Sohbeti Görüntüle</button>
                                <button className="action-button delete-button" onClick={() => handleDeleteFigure(fig.id, fig.name)}>Sil</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

export default AdminFiguresPage;