import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { Plus, Wallet, XCircle, Trash2 } from 'lucide-react';
import { feesApi, studentsApi } from '../../api/endpoints.js';
import { extractErrorMessage } from '../../api/client.js';
import { Card, CardBody } from '../../components/ui/Card.jsx';
import { Button } from '../../components/ui/Button.jsx';
import { Input, Select } from '../../components/ui/Input.jsx';
import { Table } from '../../components/ui/Table.jsx';
import { Modal } from '../../components/ui/Modal.jsx';
import { Badge } from '../../components/ui/Badge.jsx';
import { formatCurrency, formatDate } from '../../lib/utils.js';

const STATUS_TONES = {
  PENDING: 'warning',
  PARTIAL: 'info',
  PAID: 'success',
  OVERDUE: 'danger',
  WAIVED: 'default',
};

export function AdminFees() {
  const [fees, setFees] = useState([]);
  const [students, setStudents] = useState([]);
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState(false);
  const [assessOpen, setAssessOpen] = useState(false);
  const [payOpen, setPayOpen] = useState({ open: false, fee: null });
  const [form, setForm] = useState({ studentId: '', semester: '', amount: '', dueDate: '', description: '' });
  const [payAmount, setPayAmount] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      setFees(await feesApi.list(status ? { status } : {}));
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    studentsApi.list({ size: 500, sort: 'lastName' }).then((d) => setStudents(d.content)).catch(() => {});
  }, []);
  useEffect(() => { load(); /* eslint-disable-next-line */ }, [status]);

  const submitAssess = async (e) => {
    e.preventDefault();
    try {
      await feesApi.assess({
        studentId: Number(form.studentId),
        semester: form.semester,
        amount: Number(form.amount),
        dueDate: form.dueDate,
        description: form.description || null,
      });
      toast.success('Katkı payı tanımlandı');
      setAssessOpen(false);
      setForm({ studentId: '', semester: '', amount: '', dueDate: '', description: '' });
      load();
    } catch (err) {
      toast.error(extractErrorMessage(err));
    }
  };

  const submitPay = async (e) => {
    e.preventDefault();
    try {
      await feesApi.pay(payOpen.fee.id, { amount: Number(payAmount) });
      toast.success('Ödeme kaydedildi');
      setPayOpen({ open: false, fee: null });
      setPayAmount('');
      load();
    } catch (err) {
      toast.error(extractErrorMessage(err));
    }
  };

  const onWaive = async (fee) => {
    if (!confirm('Bu katkı payı muaf edilsin mi?')) return;
    try {
      await feesApi.waive(fee.id);
      toast.success('Muaf edildi');
      load();
    } catch (err) {
      toast.error(extractErrorMessage(err));
    }
  };

  const onDelete = async (fee) => {
    if (!confirm('Bu kayıt silinsin mi?')) return;
    try {
      await feesApi.delete(fee.id);
      toast.success('Silindi');
      load();
    } catch (err) {
      toast.error(extractErrorMessage(err));
    }
  };

  const columns = [
    { key: 'studentNumber', header: 'Öğrenci No' },
    { key: 'studentName', header: 'Öğrenci' },
    { key: 'semester', header: 'Dönem' },
    { key: 'amount', header: 'Tutar', render: (f) => formatCurrency(f.amount) },
    { key: 'paidAmount', header: 'Ödenen', render: (f) => formatCurrency(f.paidAmount) },
    { key: 'outstanding', header: 'Kalan', render: (f) => formatCurrency(f.outstanding) },
    { key: 'dueDate', header: 'Son Ödeme', render: (f) => formatDate(f.dueDate) },
    { key: 'status', header: 'Durum', render: (f) =>
      <Badge tone={STATUS_TONES[f.status] || 'default'}>{f.status}</Badge> },
    {
      key: 'actions', header: '', className: 'text-right',
      render: (f) => (
        <div className="flex justify-end gap-2">
          <button className="btn-ghost" onClick={() => setPayOpen({ open: true, fee: f })} title="Ödeme">
            <Wallet size={14} />
          </button>
          <button className="btn-ghost" onClick={() => onWaive(f)} title="Muaf et">
            <XCircle size={14} />
          </button>
          <button className="btn-ghost text-red-600" onClick={() => onDelete(f)} title="Sil">
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
          <h1 className="text-2xl font-bold text-slate-900">Katkı Payı</h1>
          <p className="text-sm text-slate-500">Tahakkuk, tahsilat ve muafiyetleri yönetin</p>
        </div>
        <Button onClick={() => setAssessOpen(true)}><Plus size={16} /> Yeni Tahakkuk</Button>
      </div>

      <Card>
        <CardBody className="space-y-4">
          <Select label="Duruma göre filtrele" value={status} onChange={(e) => setStatus(e.target.value)}>
            <option value="">Tümü</option>
            <option value="PENDING">Bekliyor</option>
            <option value="PARTIAL">Kısmi</option>
            <option value="PAID">Ödendi</option>
            <option value="OVERDUE">Gecikmiş</option>
            <option value="WAIVED">Muaf</option>
          </Select>
          {loading ? <div className="text-slate-500">Yükleniyor...</div>
            : <Table columns={columns} data={fees} rowKey={(r) => r.id} />}
        </CardBody>
      </Card>

      <Modal open={assessOpen} onClose={() => setAssessOpen(false)} title="Yeni Tahakkuk">
        <form id="fee-form" onSubmit={submitAssess} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Select label="Öğrenci *" required value={form.studentId}
            onChange={(e) => setForm((s) => ({ ...s, studentId: e.target.value }))}>
            <option value="">— Öğrenci seçin —</option>
            {students.map((s) => (
              <option key={s.id} value={s.id}>{s.studentNumber} - {s.firstName} {s.lastName}</option>
            ))}
          </Select>
          <Input label="Dönem *" placeholder="2025-Güz" required value={form.semester}
            onChange={(e) => setForm((s) => ({ ...s, semester: e.target.value }))} />
          <Input label="Tutar *" type="number" step="0.01" min="0" required value={form.amount}
            onChange={(e) => setForm((s) => ({ ...s, amount: e.target.value }))} />
          <Input label="Son Ödeme Tarihi *" type="date" required value={form.dueDate}
            onChange={(e) => setForm((s) => ({ ...s, dueDate: e.target.value }))} />
          <div className="sm:col-span-2">
            <Input label="Açıklama" value={form.description}
              onChange={(e) => setForm((s) => ({ ...s, description: e.target.value }))} />
          </div>
        </form>
        <div className="mt-5 flex justify-end gap-2">
          <Button variant="secondary" onClick={() => setAssessOpen(false)}>İptal</Button>
          <Button form="fee-form" type="submit">Kaydet</Button>
        </div>
      </Modal>

      <Modal open={payOpen.open} onClose={() => setPayOpen({ open: false, fee: null })}
        title={`Ödeme - ${payOpen.fee?.studentName || ''}`}>
        <form id="pay-form" onSubmit={submitPay} className="space-y-4">
          <div className="rounded-lg bg-slate-50 p-3 text-sm">
            <div>Tutar: <b>{formatCurrency(payOpen.fee?.amount)}</b></div>
            <div>Kalan: <b>{formatCurrency(payOpen.fee?.outstanding)}</b></div>
          </div>
          <Input label="Ödeme miktarı *" type="number" min="0.01" step="0.01" required
            value={payAmount} onChange={(e) => setPayAmount(e.target.value)} />
        </form>
        <div className="mt-5 flex justify-end gap-2">
          <Button variant="secondary" onClick={() => setPayOpen({ open: false, fee: null })}>İptal</Button>
          <Button form="pay-form" type="submit">Tahsil Et</Button>
        </div>
      </Modal>
    </div>
  );
}
