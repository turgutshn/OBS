import React, { useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { LockKeyhole, ShieldCheck } from 'lucide-react';
import { useAuth } from '../auth/AuthContext.jsx';
import { extractErrorMessage } from '../api/client.js';
import { Input } from '../components/ui/Input.jsx';
import { Button } from '../components/ui/Button.jsx';

export function Login() {
  const { isAuthenticated, login, user, bootstrapping } = useAuth();
  const [credentials, setCredentials] = useState({ username: '', password: '' });
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname;

  if (bootstrapping) return null;

  if (isAuthenticated) {
    const home = user?.role === 'STUDENT' ? '/student' : '/admin';
    return <Navigate to={from && from !== '/login' ? from : home} replace />;
  }

  const onSubmit = async (e) => {
    e.preventDefault();
    if (submitting) return;
    setSubmitting(true);
    try {
      const data = await login(credentials.username.trim(), credentials.password);
      toast.success('Giriş başarılı');
      const home = data.user?.role === 'STUDENT' ? '/student' : '/admin';
      navigate(home, { replace: true });
    } catch (err) {
      toast.error(extractErrorMessage(err, 'Giriş başarısız'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-brand-50 via-slate-50 to-white p-4">
      <div className="w-full max-w-md">
        <div className="mb-8 text-center">
          <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-xl bg-brand-600 text-white shadow-lg shadow-brand-600/30">
            <ShieldCheck size={26} />
          </div>
          <h1 className="text-2xl font-bold text-slate-900">OBS</h1>
          <p className="mt-1 text-sm text-slate-500">Öğrenci Yönetim Sistemine giriş yapın</p>
        </div>

        <div className="card">
          <div className="card-body">
            <form onSubmit={onSubmit} className="space-y-4">
              <Input
                label="Kullanıcı adı"
                name="username"
                autoComplete="username"
                value={credentials.username}
                onChange={(e) => setCredentials((s) => ({ ...s, username: e.target.value }))}
                required
                autoFocus
              />
              <Input
                label="Şifre"
                name="password"
                type="password"
                autoComplete="current-password"
                value={credentials.password}
                onChange={(e) => setCredentials((s) => ({ ...s, password: e.target.value }))}
                required
              />
              <Button type="submit" disabled={submitting} className="w-full">
                <LockKeyhole size={16} />
                {submitting ? 'Giriş yapılıyor...' : 'Giriş Yap'}
              </Button>
            </form>
          </div>
        </div>

        <p className="mt-6 text-center text-xs text-slate-400">
          Güvenli giriş • JWT + Refresh Token • Şifreler bcrypt ile saklanır
        </p>
      </div>
    </div>
  );
}
