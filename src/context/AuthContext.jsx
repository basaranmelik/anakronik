import React, { createContext, useState, useContext, useEffect } from 'react';
import apiClient from '../api/axiosConfig';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    const fetchUserProfile = async () => {
        try {
            const response = await apiClient.get('/users/profile');
            setUser(response.data);
        } catch (error) {
            console.error("Profil alınamadı, token geçersiz veya süresi dolmuş olabilir.", error);
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            setUser(null);
        }
    };

    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        if (token) {
            fetchUserProfile().finally(() => setLoading(false));
        } else {
            setLoading(false);
        }
    }, []);

    const login = async (email, password) => {
        try {
            const response = await apiClient.post('/auth/login', { email, password });
            const { accessToken, refreshToken } = response.data;
            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('refreshToken', refreshToken);

            await fetchUserProfile();

            return true;
        } catch (error) {
            console.error("Login Hatası:", error);
            return false;
        }
    };

    const register = async (fullName, email, password) => {
        return apiClient.post('/auth/register', { fullName, email, password });
    };

    const logout = () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        setUser(null);
    };
    const value = { user, login, logout, register, isAuthenticated: !!user };

    if (loading) {
        return <div>Oturum kontrol ediliyor...</div>;
    }

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
    return useContext(AuthContext);
};