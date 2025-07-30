import React from 'react';
import { Routes, Route } from 'react-router-dom';
import HomePage from '../pages/HomePage';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import MapPage from '../pages/MapPage';
import CreateFigurePage from '../pages/CreateFigurePage';
import ChatPage from '../pages/ChatPage';
import ProtectedRoute from '../components/ProtectedRoute';

const AppRoutes = () => {
  return (
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/map" element={<ProtectedRoute><MapPage /></ProtectedRoute>} />
        <Route path="/create-figure" element={<ProtectedRoute><CreateFigurePage /></ProtectedRoute>} />
        <Route path="/chat/:figureId" element={<ProtectedRoute><ChatPage /></ProtectedRoute>} />
      </Routes>
  );
};

export default AppRoutes;
