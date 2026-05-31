import React, { useCallback, useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { Plus, Search, Pencil, Trash2 } from 'lucide-react';
import { coursesApi, teachersApi } from '../../api/endpoints.js';
import { extractErrorMessage } from '../../api/client.js';
import { Card, CardBody } from '../../components/ui/Card.jsx';
import { Button } from '../../components/ui/Button.jsx';
import { Input, Select, TextArea } from '../../components/ui/Input.jsx';
import { Table } from '../../components/ui/Table.jsx';
import { Modal } from '../../components/ui/Modal.jsx';
import { Badge } from '../../components/ui/Badge.jsx';

const EMPTY = {
  code: '', name: '', description: '', credits: 3,
  department: '', semester: '', teacherId: '', active: true,
};

export function AdminCourses() {
  const [page, setPage] = useState({ content: [], totalPages: 0, page: 0 });
  const [teachers, setTeachers] = useState([]);
  const [q, setQ] = useState('');
  const [loading, setLoading] = useState(false);
  const [modal, setModal] = useState({ open: false, editing: null });
  const [form, setForm] = useState(EMPTY);

  const load = useCallback(async (p = 0, search = q) => {
    setLoading(true);
    try {
      const data = await coursesApi.list({ q: search, page: p, size: 20, sort: 'code' });
      setPage(data);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [q]);

  useEffect(() => {
    load(0, '');
    teachersApi.list({ size: 200, sort: 'lastName' }).then((d) => setTeachers(d.content)).catch(() => {});
  }, [load]);

  const openCreate = () => { setForm(EMPTY); setModal({ open: true, editing: null }); };
  const openEdit = (c) => {
    setForm({
      code: c.code, name: c.name, description: c.description || '',
      credits: c.credits, department: c.department || '',
      semester: c.semester || '',
      teacherId: c.teacherId || '',
      active: c.active,
    });
    setModal({ open: true, editing: c });
  };

  const submit = async (e) => {
    e.preventDefault();
    const payload = {
      ...form,
      credits: Number(form.credits),
      teacherId: form.teacherId ? Number(form.teacherId) : null,
    };
    try {
      if (modal.editing) await coursesApi.update(modal.editing.id, payload);
      else await coursesApi.create(payload);
      toast.success('Ders kaydedildi');
      setModal({ open: false, editing: null });
      load(page.page);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    }
  };

  const onDelete = async (c) => {
    if (!confirm(`${c.code} silinsin mi?`)) return;
    try {
      await coursesApi.delete(c.id);
      toast.success('Ders silindi');
      load(page.page);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    }
  };

  const columns = [
    { key: 'code', header: 'Kod' },
    { key: 'name', header: 'Ders Adı' },
    { key: 'credits', header: 'Kredi' },
    { key: 'department', header: 'Bölüm', render: (c) => c.department || '-' },
    { key: 'semester', header: 'Dönem', render: (c) => c.semester || '-' },
    { key: 'teacher', header: 'Öğretmen', render: (c) => c.teacherName || '-' },
    { key: 'active', header: 'Durum', render: (c) =>
      <Badge tone={c.active ? 'success' : 'default'}>{c.active ? 'Aktif' : 'Pasif'}</Badge> },
    {
      key: 'actions', header: '', className: 'text-right',
      render: (c) => (
        <div className="flex justify-end gap-2">
          <button className="btn-ghost" onClick={() => openEdit(c)}><Pencil size={14} /></button>
          <button className="btn-ghost text-red-600" onClick={() => onDelete(c)}><Trash2 size={14} /></button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Dersler</h1>
          <p className="text-sm text-slate-500">Ders tanımları ve öğretmen atamaları</p>
        </div>
        <Button onClick={openCreate}><Plus size={16} /> Yeni Ders</Button>
      </div>

      <Card>
        <CardBody className="space-y-4">
          <form onSubmit={(e) => { e.preventDefault(); load(0); }} className="flex gap-3">
            <Input placeholder="Ders kodu, adı, bölüm..." value={q}
              onChange={(e) => setQ(e.target.value)} className="max-w-md" />
            <Button variant="secondary" type="submit"><Search size={14} /> Ara</Button>
          </form>
          {loading ? <div className="text-slate-500">Yükleniyor...</div>
            : <Table columns={columns} data={page.content} rowKey={(r) => r.id} />}
        </CardBody>
      </Card>

      <Modal open={modal.open} onClose={() => setModal({ open: false, editing: null })}
        title={modal.editing ? 'Dersi Düzenle' : 'Yeni Ders'} size="lg">
        <form id="course-form" onSubmit={submit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Input label="Kod *" required value={form.code}
            onChange={(e) => setForm((s) => ({ ...s, code: e.target.value }))} />
          <Input label="Ad *" required value={form.name}
            onChange={(e) => setForm((s) => ({ ...s, name: e.target.value }))} />
          <Input label="Kredi *" required type="number" min="1" max="12" value={form.credits}
            onChange={(e) => setForm((s) => ({ ...s, credits: e.target.value }))} />
          <Input label="Bölüm" value={form.department}
            onChange={(e) => setForm((s) => ({ ...s, department: e.target.value }))} />
          <Input label="Dönem" placeholder="2025-Güz" value={form.semester}
            onChange={(e) => setForm((s) => ({ ...s, semester: e.target.value }))} />
          <Select label="Öğretmen" value={form.teacherId}
            onChange={(e) => setForm((s) => ({ ...s, teacherId: e.target.value }))}>
            <option value="">— Atanmadı —</option>
            {teachers.map((t) => (
              <option key={t.id} value={t.id}>{t.firstName} {t.lastName} ({t.employeeNumber})</option>
            ))}
          </Select>
          <div className="sm:col-span-2">
            <TextArea label="Açıklama" value={form.description}
              onChange={(e) => setForm((s) => ({ ...s, description: e.target.value }))} />
          </div>
          <label className="flex items-center gap-2 text-sm">
            <input type="checkbox" checked={form.active}
              onChange={(e) => setForm((s) => ({ ...s, active: e.target.checked }))} />
            Aktif
          </label>
        </form>
        <div className="mt-5 flex justify-end gap-2">
          <Button variant="secondary" onClick={() => setModal({ open: false, editing: null })}>İptal</Button>
          <Button form="course-form" type="submit">{modal.editing ? 'Güncelle' : 'Oluştur'}</Button>
        </div>
      </Modal>
    </div>
  );
}
