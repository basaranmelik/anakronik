import React, { createContext, useState, useContext, useEffect } from 'react';
import apiClient from '../api/axiosConfig';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    // State'i sadece token var mı yok mu diye tutmak yerine, kullanıcı bilgisi için de kullanabiliriz.
    // Şimdilik basit tutarak sadece token varlığını kontrol edelim.
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Uygulama ilk yüklendiğinde token var mı diye kontrol et.
        const storedToken = localStorage.getItem('accessToken');
        if (storedToken) {
            setIsAuthenticated(true);
        }
        setLoading(false);
    }, []);

    const login = async (email, password) => {
        try {
            const response = await apiClient.post('/auth/login', { email, password });

            // --- DÜZELTME: Hem accessToken hem de refreshToken'ı al ---
            const { accessToken, refreshToken } = response.data;

            if (accessToken && refreshToken) {
                // --- DÜZELTME: Her iki token'ı da localStorage'a kaydet ---
                localStorage.setItem('accessToken', accessToken);
                localStorage.setItem('refreshToken', refreshToken);
                setIsAuthenticated(true);
                return true;
            }
            return false;
        } catch (error) {
            console.error("Login Error:", error);
            return false;
        }
    };

    const register = async (fullName, email, password) => {
        try {
            await apiClient.post('/auth/register', { fullName, email, password });
            return true;
        } catch (error) {
            console.error("Register Error:", error);
            return false;
        }
    };

    const logout = () => {
        // --- DÜZELTME: Her iki token'ı da localStorage'dan sil ---
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        setIsAuthenticated(false);
    };

    // Context değerini oluştur
    const value = {
        isAuthenticated,
        loading,
        login,
        logout,
        register
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// Kendi hook'unuzu koruyoruz, bu iyi bir pratik.
export const useAuth = () => {
    return useContext(AuthContext);
};
