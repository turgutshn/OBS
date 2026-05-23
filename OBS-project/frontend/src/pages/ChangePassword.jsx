import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { authApi } from '../api/endpoints.js';
import { extractErrorMessage } from '../api/client.js';
import { Card, CardBody, CardHeader } from '../components/ui/Card.jsx';
import { Input } from '../components/ui/Input.jsx';
import { Button } from '../components/ui/Button.jsx';
import { useAuth } from '../auth/AuthContext.jsx';

export function ChangePassword() {
  const [form, setForm] = useState({ currentPassword: '', newPassword: '', confirm: '' });
  const [submitting, setSubmitting] = useState(false);
  const { logout } = useAuth();
  const navigate = useNavigate();

  const onSubmit = async (e) => {
    e.preventDefault();
    if (form.newPassword !== form.confirm) {
      toast.error('Yeni şifreler eşleşmiyor');
      return;
    }
    setSubmitting(true);
    try {
      await authApi.changePassword(form.currentPassword, form.newPassword);
      toast.success('Şifre değiştirildi. Lütfen tekrar giriş yapın.');
      await logout();
      navigate('/login', { replace: true });
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-lg">
      <Card>
        <CardHeader title="Şifre Değiştir" subtitle="Güvenliğiniz için güçlü bir şifre seçin" />
        <CardBody>
          <form onSubmit={onSubmit} className="space-y-4">
            <Input label="Mevcut şifre" type="password" required
              value={form.currentPassword}
              onChange={(e) => setForm((s) => ({ ...s, currentPassword: e.target.value }))} />
            <Input label="Yeni şifre" type="password" required minLength={8}
              value={form.newPassword}
              onChange={(e) => setForm((s) => ({ ...s, newPassword: e.target.value }))} />
            <Input label="Yeni şifre (tekrar)" type="password" required minLength={8}
              value={form.confirm}
              onChange={(e) => setForm((s) => ({ ...s, confirm: e.target.value }))} />
            <Button type="submit" disabled={submitting} className="w-full">
              {submitting ? 'Kaydediliyor...' : 'Şifreyi Güncelle'}
            </Button>
          </form>
        </CardBody>
      </Card>
    </div>
  );
}
