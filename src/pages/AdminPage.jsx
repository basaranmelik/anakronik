// src/pages/AdminPage.jsx
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import './AdminPage.css'; // Yeni CSS dosyamız

function AdminPage() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        fetchUsers();
    }, []);

    const fetchUsers = () => {
        setLoading(true);
        apiClient.get('/admin/users')
            .then(response => {
                setUsers(response.data.content || []);
                setLoading(false);
            })
            .catch(err => {
                console.error("Kullanıcılar çekilirken hata:", err);
                setError("Kullanıcı listesi yüklenemedi.");
                setLoading(false);
            });
    };

    const handleDeleteUser = async (userId, userEmail) => {
        if (window.confirm(`'${userEmail}' kullanıcısını silmek istediğinize emin misiniz?`)) {
            try {
                await apiClient.delete(`/admin/users/${userId}`);
                // Listeyi yeniden çekerek güncelle
                fetchUsers();
            } catch (err) {
                console.error("Kullanıcı silinirken hata:", err);
                alert("Kullanıcı silinirken bir hata oluştu.");
            }
        }
    };

    if (loading) return <div className="admin-page-container"><h2>Yükleniyor...</h2></div>;
    if (error) return <div className="admin-page-container"><h2>{error}</h2></div>;

    return (
        <div className="admin-page-container">
            <header className="admin-header">
                <h1>Admin Paneli - Kullanıcı Yönetimi</h1>
                <Link to="/map" className="back-link">← Haritaya Geri Dön</Link>
            </header>
            <table className="users-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Email</th>
                        <th>Tam İsim</th>
                        <th>Rol</th>
                        <th>İşlemler</th>
                    </tr>
                </thead>
                <tbody>
                    {users.map(user => (
                        <tr key={user.id}>
                            <td>{user.id}</td>
                            <td>{user.email}</td>
                            <td>{user.fullName}</td>
                            <td>{user.roles.join(', ')}</td>
                            <td>
                                <button className="action-button edit-button">Düzenle</button>
                                <button className="action-button delete-button" onClick={() => handleDeleteUser(user.id, user.email)}>Sil</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

export default AdminPage;