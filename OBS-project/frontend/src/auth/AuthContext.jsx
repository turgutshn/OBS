import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/endpoints.js';
import { readAuth, setUnauthenticatedHandler, writeAuth } from '../api/client.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => readAuth());
  const [bootstrapping, setBootstrapping] = useState(true);
  const navigate = useNavigate();

  const logout = useCallback(async () => {
    const current = readAuth();
    try {
      if (current?.refreshToken) {
        await authApi.logout(current.refreshToken);
      }
    } catch { /* ignore */ }
    writeAuth(null);
    setAuth(null);
    navigate('/login', { replace: true });
  }, [navigate]);

  useEffect(() => {
    setUnauthenticatedHandler(() => {
      writeAuth(null);
      setAuth(null);
      navigate('/login', { replace: true });
    });
  }, [navigate]);

  useEffect(() => {
    let mounted = true;
    async function init() {
      const stored = readAuth();
      if (stored?.accessToken) {
        try {
          const me = await authApi.me();
          if (mounted) {
            setAuth({ ...stored, user: me });
            writeAuth({ ...stored, user: me });
          }
        } catch {
          writeAuth(null);
          if (mounted) setAuth(null);
        }
      }
      if (mounted) setBootstrapping(false);
    }
    init();
    return () => { mounted = false; };
  }, []);

  const login = useCallback(async (username, password) => {
    const data = await authApi.login(username, password);
    writeAuth(data);
    setAuth(data);
    return data;
  }, []);

  const value = useMemo(() => ({
    auth,
    user: auth?.user,
    isAuthenticated: Boolean(auth?.accessToken),
    bootstrapping,
    login,
    logout,
  }), [auth, bootstrapping, login, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
}
