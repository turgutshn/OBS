import React, { useCallback, useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { Plus, Search, Pencil, Trash2 } from 'lucide-react';
import { teachersApi } from '../../api/endpoints.js';
import { extractErrorMessage } from '../../api/client.js';
import { Card, CardBody } from '../../components/ui/Card.jsx';
import { Button } from '../../components/ui/Button.jsx';
import { Input } from '../../components/ui/Input.jsx';
import { Table } from '../../components/ui/Table.jsx';
import { Modal } from '../../components/ui/Modal.jsx';
import { Badge } from '../../components/ui/Badge.jsx';

const EMPTY = {
  employeeNumber: '', firstName: '', lastName: '', email: '',
  password: '', title: '', department: '', phone: '',
};

export function AdminTeachers() {
  const [page, setPage] = useState({ content: [], totalPages: 0, page: 0 });
  const [q, setQ] = useState('');
  const [loading, setLoading] = useState(false);
  const [modal, setModal] = useState({ open: false, editing: null });
  const [form, setForm] = useState(EMPTY);

  const load = useCallback(async (p = 0, search = q) => {
    setLoading(true);
    try {
      const data = await teachersApi.list({ q: search, page: p, size: 20, sort: 'lastName' });
      setPage(data);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [q]);

  useEffect(() => { load(0, ''); }, [load]);

  const openCreate = () => { setForm(EMPTY); setModal({ open: true, editing: null }); };
  const openEdit = (t) => {
    setForm({
      employeeNumber: t.employeeNumber,
      firstName: t.firstName,
      lastName: t.lastName,
      email: t.email,
      password: '',
      title: t.title || '',
      department: t.department || '',
      phone: t.phone || '',
    });
    setModal({ open: true, editing: t });
  };

  const submit = async (e) => {
    e.preventDefault();
    const payload = { ...form, password: form.password || undefined };
    try {
      if (modal.editing) {
        await teachersApi.update(modal.editing.id, payload);
        toast.success('Öğretmen güncellendi');
      } else {
        await teachersApi.create(payload);
        toast.success('Öğretmen oluşturuldu');
      }
      setModal({ open: false, editing: null });
      load(page.page);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    }
  };

  const onDelete = async (t) => {
    if (!confirm(`${t.firstName} ${t.lastName} silinsin mi?`)) return;
    try {
      await teachersApi.delete(t.id);
      toast.success('Öğretmen silindi');
      load(page.page);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    }
  };

  const columns = [
    { key: 'employeeNumber', header: 'Personel No' },
    { key: 'name', header: 'Ad Soyad', render: (t) => `${t.title || ''} ${t.firstName} ${t.lastName}`.trim() },
    { key: 'email', header: 'E-posta' },
    { key: 'department', header: 'Bölüm', render: (t) => t.department || '-' },
    { key: 'status', header: 'Durum', render: (t) =>
      <Badge tone={t.active ? 'success' : 'danger'}>{t.active ? 'Aktif' : 'Pasif'}</Badge> },
    {
      key: 'actions', header: '', className: 'text-right',
      render: (t) => (
        <div className="flex justify-end gap-2">
          <button className="btn-ghost" onClick={() => openEdit(t)}><Pencil size={14} /></button>
          <button className="btn-ghost text-red-600" onClick={() => onDelete(t)}><Trash2 size={14} /></button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Öğretmenler</h1>
          <p className="text-sm text-slate-500">Öğretim elemanlarını yönetin</p>
        </div>
        <Button onClick={openCreate}><Plus size={16} /> Yeni Öğretmen</Button>
      </div>

      <Card>
        <CardBody className="space-y-4">
          <form onSubmit={(e) => { e.preventDefault(); load(0); }} className="flex gap-3">
            <Input placeholder="Ad, soyad, personel no..." value={q}
              onChange={(e) => setQ(e.target.value)} className="max-w-md" />
            <Button variant="secondary" type="submit"><Search size={14} /> Ara</Button>
          </form>
          {loading ? <div className="text-slate-500">Yükleniyor...</div>
            : <Table columns={columns} data={page.content} rowKey={(r) => r.id} />}
        </CardBody>
      </Card>

      <Modal open={modal.open} onClose={() => setModal({ open: false, editing: null })}
        title={modal.editing ? 'Öğretmeni Düzenle' : 'Yeni Öğretmen'} size="lg">
        <form id="teacher-form" onSubmit={submit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Input label="Personel No *" required value={form.employeeNumber}
            onChange={(e) => setForm((s) => ({ ...s, employeeNumber: e.target.value }))} />
          <Input label="E-posta *" type="email" required value={form.email}
            onChange={(e) => setForm((s) => ({ ...s, email: e.target.value }))} />
          <Input label="Ad *" required value={form.firstName}
            onChange={(e) => setForm((s) => ({ ...s, firstName: e.target.value }))} />
          <Input label="Soyad *" required value={form.lastName}
            onChange={(e) => setForm((s) => ({ ...s, lastName: e.target.value }))} />
          <Input label="Unvan" placeholder="Prof. Dr., Doç. Dr., Dr. Öğr. Üyesi..." value={form.title}
            onChange={(e) => setForm((s) => ({ ...s, title: e.target.value }))} />
          <Input label="Bölüm" value={form.department}
            onChange={(e) => setForm((s) => ({ ...s, department: e.target.value }))} />
          <Input label="Telefon" value={form.phone}
            onChange={(e) => setForm((s) => ({ ...s, phone: e.target.value }))} />
          <Input
            label={modal.editing ? 'Yeni şifre (opsiyonel)' : 'Başlangıç şifresi (opsiyonel)'}
            type="password" minLength={8}
            value={form.password}
            onChange={(e) => setForm((s) => ({ ...s, password: e.target.value }))}
          />
        </form>
        <div className="mt-5 flex justify-end gap-2">
          <Button variant="secondary" onClick={() => setModal({ open: false, editing: null })}>İptal</Button>
          <Button form="teacher-form" type="submit">{modal.editing ? 'Güncelle' : 'Oluştur'}</Button>
        </div>
      </Modal>
    </div>
  );
}
