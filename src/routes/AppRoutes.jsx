import React from 'react';
import { Routes, Route } from 'react-router-dom';
import HomePage from '../pages/HomePage';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import MapPage from '../pages/MapPage';
import CreateFigurePage from '../pages/CreateFigurePage';
import ChatPage from '../pages/ChatPage';
import ProtectedRoute from '../components/ProtectedRoute';
import ForgotPasswordPage from '../pages/ForgotPasswordPage';
import ResetPasswordPage from '../pages/ResetPasswordPage';

const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/map" element={<ProtectedRoute><MapPage /></ProtectedRoute>} />
      <Route path="/create-figure" element={<ProtectedRoute><CreateFigurePage /></ProtectedRoute>} />
      <Route path="/chat/:figureId" element={<ProtectedRoute><ChatPage /></ProtectedRoute>} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />
    </Routes>
  );
};

export default AppRoutes;
