import React, { createContext, useState, useContext, useEffect } from 'react';
import apiClient from '../api/axiosConfig';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedToken = localStorage.getItem('accessToken');
    if (storedToken) {
      setToken(storedToken);
    }
    setLoading(false); // <-- KONTROL BİTTİĞİNDE YÜKLENMEYİ BİTİR
  }, []);

  const login = async (email, password) => {
    try {
      const response = await apiClient.post('/auth/login', { email, password });
      const accessToken = response.data.accessToken;
      localStorage.setItem('accessToken', accessToken);
      setToken(accessToken);
      return true; // Başarılı
    } catch (error) {
      console.error("Login Error:", error);
      return false; // Başarısız
    }
  };

  const register = async (fullName, email, password) => {
    try {
      await apiClient.post('/auth/register', { fullName, email, password });
      return true; // Başarılı
    } catch (error) {
      console.error("Register Error:", error);
      return false; // Başarısız
    }
  };

  const logout = () => {
    localStorage.removeItem('accessToken');
    setToken(null);
  };

  const value = { token, loading, login, logout, isAuthenticated: !!token };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  return useContext(AuthContext);
};