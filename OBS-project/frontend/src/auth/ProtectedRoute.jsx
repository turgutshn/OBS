import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext.jsx';

export function ProtectedRoute({ children, roles }) {
  const { isAuthenticated, user, bootstrapping } = useAuth();
  const location = useLocation();

  if (bootstrapping) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-slate-500">Yükleniyor...</div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (roles && roles.length && user && !roles.includes(user.role)) {
    return <Navigate to="/" replace />;
  }

  return children;
}
