// src/context/AuthContext.jsx
import React, { createContext, useState, useContext, useEffect } from 'react';
import apiClient from '../api/axiosConfig';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    // Sadece boolean değil, backend'den gelen tüm kullanıcı objesini tutacağız
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true); // Sayfa ilk yüklendiğinde oturum kontrolü için

    // Token'a göre kullanıcı profilini (roller dahil) çeken fonksiyon
    const fetchUserProfile = async () => {
        try {
            const response = await apiClient.get('/users/profile');
            setUser(response.data); // Gelen kullanıcı verisini (rolleriyle birlikte) state'e yaz
        } catch (error) {
            console.error("Profil alınamadı, token geçersiz veya süresi dolmuş olabilir.", error);
            // Hata olursa, eski token'ı temizle ve kullanıcıyı sistemden at
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            setUser(null);
        }
    };

    // Sayfa ilk yüklendiğinde token var mı diye kontrol et
    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        if (token) {
            // Token varsa, kullanıcının bilgilerini çekerek oturumu doğrula
            fetchUserProfile().finally(() => setLoading(false));
        } else {
            setLoading(false); // Token yoksa yüklemeyi bitir, misafir olarak devam et
        }
    }, []);

    const login = async (email, password) => {
        try {
            const response = await apiClient.post('/auth/login', { email, password });
            const { accessToken, refreshToken } = response.data;
            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('refreshToken', refreshToken);

            // Giriş başarılı olduktan sonra hemen kullanıcı bilgilerini ve rollerini çek
            await fetchUserProfile();

            return true;
        } catch (error) {
            console.error("Login Hatası:", error);
            return false;
        }
    };

    const logout = () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        setUser(null);
    };

    // Context'e artık sadece boolean değil, tüm user objesini veriyoruz
    const value = { user, login, logout, isAuthenticated: !!user };

    // Oturum durumu netleşene kadar (yükleme bitene kadar) uygulamayı beklet
    if (loading) {
        return <div>Oturum kontrol ediliyor...</div>;
    }

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
    return useContext(AuthContext);
};