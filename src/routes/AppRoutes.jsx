import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import HomePage from '../pages/HomePage';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import MapPage from '../pages/MapPage';
import CreateFigurePage from '../pages/CreateFigurePage';
import ChatPage from '../pages/ChatPage';
import ProtectedRoute from '../components/ProtectedRoute';
import ForgotPasswordPage from '../pages/ForgotPasswordPage';
import ResetPasswordPage from '../pages/ResetPasswordPage';
import ProfilePage from '../pages/ProfilePage';
import AdminPage from '../pages/AdminPage';
import AdminFiguresPage from '../pages/AdminFiguresPage';
import ErrorPage from '../pages/ErrorPage';

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
      <Route
        path="/profile"
        element={
          <ProtectedRoute>
            <ProfilePage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/users"
        element={
          <ProtectedRoute roles={['ROLE_ADMIN']}>
            <AdminPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/figures"
        element={<ProtectedRoute roles={['ROLE_ADMIN']}> <AdminFiguresPage /> </ProtectedRoute>}
      />
      <Route path="/error-page" element={<ErrorPage />} />
      <Route path="*" element={<ErrorPage />} />
    </Routes>
  );
};

export default AppRoutes;
