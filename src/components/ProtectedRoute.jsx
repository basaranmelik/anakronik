// src/components/ProtectedRoute.jsx
import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth(); // <-- loading'i context'ten al
  const location = useLocation();

  // EĞER OTURUM BİLGİSİ HENÜZ YÜKLENİYORSA, BEKLE (BİR ŞEY GÖSTERME)
  if (loading) {
    return null; // Veya bir "Yükleniyor..." spinner'ı gösterebilirsin
  }

  // YÜKLENME BİTTİKTEN SONRA, GİRİŞ YAPILMAMIŞSA YÖNLENDİR
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // GİRİŞ YAPILMIŞSA, SAYFAYI GÖSTER
  return children;
};

export default ProtectedRoute;