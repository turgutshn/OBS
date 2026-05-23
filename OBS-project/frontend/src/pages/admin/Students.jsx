import React, { useCallback, useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { Plus, Search, Pencil, Trash2, ScrollText } from 'lucide-react';
import { Link } from 'react-router-dom';
import { studentsApi } from '../../api/endpoints.js';
import { extractErrorMessage } from '../../api/client.js';
import { Card, CardBody, CardHeader } from '../../components/ui/Card.jsx';
import { Button } from '../../components/ui/Button.jsx';
import { Input } from '../../components/ui/Input.jsx';
import { Table } from '../../components/ui/Table.jsx';
import { Modal } from '../../components/ui/Modal.jsx';
import { Badge } from '../../components/ui/Badge.jsx';

const EMPTY_FORM = {
  studentNumber: '', firstName: '', lastName: '', email: '', password: '',
  nationalId: '', phone: '', dateOfBirth: '', department: '', enrollmentYear: '', address: '',
};

export function AdminStudents() {
  const [page, setPage] = useState({ content: [], totalPages: 0, page: 0 });
  const [q, setQ] = useState('');
  const [loading, setLoading] = useState(false);
  const [modal, setModal] = useState({ open: false, editing: null });
  const [form, setForm] = useState(EMPTY_FORM);

  const load = useCallback(async (pageNumber = 0, search = q) => {
    setLoading(true);
    try {
      const data = await studentsApi.list({ q: search, page: pageNumber, size: 20, sort: 'lastName' });
      setPage(data);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [q]);

  useEffect(() => { load(0, ''); /* initial */ }, [load]);

  const openCreate = () => {
    setForm(EMPTY_FORM);
    setModal({ open: true, editing: null });
  };

  const openEdit = (s) => {
    setForm({
      studentNumber: s.studentNumber,
      firstName: s.firstName,
      lastName: s.lastName,
      email: s.email,
      password: '',
      nationalId: s.nationalId || '',
      phone: s.phone || '',
      dateOfBirth: s.dateOfBirth || '',
      department: s.department || '',
      enrollmentYear: s.enrollmentYear || '',
      address: s.address || '',
    });
    setModal({ open: true, editing: s });
  };

  const submit = async (e) => {
    e.preventDefault();
    const payload = {
      ...form,
      enrollmentYear: form.enrollmentYear ? Number(form.enrollmentYear) : null,
      password: form.password || undefined,
    };
    try {
      if (modal.editing) {
        await studentsApi.update(modal.editing.id, payload);
        toast.success('Öğrenci güncellendi');
      } else {
        await studentsApi.create(payload);
        toast.success('Öğrenci oluşturuldu');
      }
      setModal({ open: false, editing: null });
      load(page.page);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    }
  };

  const onDelete = async (s) => {
    if (!confirm(`${s.firstName} ${s.lastName} silinsin mi?`)) return;
    try {
      await studentsApi.delete(s.id);
      toast.success('Öğrenci silindi');
      load(page.page);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    }
  };

  const columns = [
    { key: 'studentNumber', header: 'Öğrenci No' },
    { key: 'name', header: 'Ad Soyad', render: (s) => `${s.firstName} ${s.lastName}` },
    { key: 'email', header: 'E-posta' },
    { key: 'department', header: 'Bölüm', render: (s) => s.department || '-' },
    { key: 'enrollmentYear', header: 'Kayıt Yılı', render: (s) => s.enrollmentYear || '-' },
    { key: 'status', header: 'Durum', render: (s) => (
      <Badge tone={s.active ? 'success' : 'danger'}>{s.active ? 'Aktif' : 'Pasif'}</Badge>
    )},
    {
      key: 'actions',
      header: '',
      className: 'text-right',
      render: (s) => (
        <div className="flex justify-end gap-2">
          <Link to={`/admin/transcript/${s.id}`} className="btn-ghost" title="Transkript">
            <ScrollText size={14} />
          </Link>
          <button className="btn-ghost" onClick={() => openEdit(s)} title="Düzenle">
            <Pencil size={14} />
          </button>
          <button className="btn-ghost text-red-600" onClick={() => onDelete(s)} title="Sil">
            <Trash2 size={14} />
          </button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Öğrenciler</h1>
          <p className="text-sm text-slate-500">Öğrenci ekleyin, düzenleyin, arayın veya silin</p>
        </div>
        <Button onClick={openCreate}>
          <Plus size={16} /> Yeni Öğrenci
        </Button>
      </div>

      <Card>
        <CardBody className="space-y-4">
          <form onSubmit={(e) => { e.preventDefault(); load(0); }} className="flex gap-3">
            <Input
              placeholder="Ad, soyad, öğrenci no, bölüm..."
              value={q}
              onChange={(e) => setQ(e.target.value)}
              className="max-w-md"
            />
            <Button type="submit" variant="secondary"><Search size={14} /> Ara</Button>
          </form>

          {loading ? (
            <div className="text-slate-500">Yükleniyor...</div>
          ) : (
            <>
              <Table columns={columns} data={page.content} rowKey={(r) => r.id} />
              {page.totalPages > 1 && (
                <div className="flex items-center justify-between text-sm">
                  <span className="text-slate-500">
                    Sayfa {page.page + 1} / {page.totalPages} · Toplam {page.totalElements}
                  </span>
                  <div className="flex gap-2">
                    <Button variant="secondary" disabled={page.page === 0} onClick={() => load(page.page - 1)}>Önceki</Button>
                    <Button variant="secondary" disabled={page.page + 1 >= page.totalPages} onClick={() => load(page.page + 1)}>Sonraki</Button>
                  </div>
                </div>
              )}
            </>
          )}
        </CardBody>
      </Card>

      <Modal
        open={modal.open}
        onClose={() => setModal({ open: false, editing: null })}
        title={modal.editing ? 'Öğrenciyi Düzenle' : 'Yeni Öğrenci'}
        size="lg"
      >
        <form id="student-form" onSubmit={submit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Input label="Öğrenci No *" required value={form.studentNumber}
            onChange={(e) => setForm((s) => ({ ...s, studentNumber: e.target.value }))} />
          <Input label="E-posta *" type="email" required value={form.email}
            onChange={(e) => setForm((s) => ({ ...s, email: e.target.value }))} />
          <Input label="Ad *" required value={form.firstName}
            onChange={(e) => setForm((s) => ({ ...s, firstName: e.target.value }))} />
          <Input label="Soyad *" required value={form.lastName}
            onChange={(e) => setForm((s) => ({ ...s, lastName: e.target.value }))} />
          <Input label="TC Kimlik No" value={form.nationalId}
            onChange={(e) => setForm((s) => ({ ...s, nationalId: e.target.value }))} />
          <Input label="Telefon" value={form.phone}
            onChange={(e) => setForm((s) => ({ ...s, phone: e.target.value }))} />
          <Input label="Doğum Tarihi" type="date" value={form.dateOfBirth}
            onChange={(e) => setForm((s) => ({ ...s, dateOfBirth: e.target.value }))} />
          <Input label="Bölüm" value={form.department}
            onChange={(e) => setForm((s) => ({ ...s, department: e.target.value }))} />
          <Input label="Kayıt Yılı" type="number" min="1900" max="2100" value={form.enrollmentYear}
            onChange={(e) => setForm((s) => ({ ...s, enrollmentYear: e.target.value }))} />
          <Input
            label={modal.editing ? 'Yeni şifre (boş bırakırsanız değişmez)' : 'Başlangıç şifresi (boş bırakırsanız rastgele üretilir)'}
            type="password"
            minLength={8}
            value={form.password}
            onChange={(e) => setForm((s) => ({ ...s, password: e.target.value }))}
          />
          <div className="sm:col-span-2">
            <Input label="Adres" value={form.address}
              onChange={(e) => setForm((s) => ({ ...s, address: e.target.value }))} />
          </div>
        </form>
        <div className="mt-5 flex justify-end gap-2">
          <Button variant="secondary" onClick={() => setModal({ open: false, editing: null })}>İptal</Button>
          <Button form="student-form" type="submit">{modal.editing ? 'Güncelle' : 'Oluştur'}</Button>
        </div>
      </Modal>
    </div>
  );
}
