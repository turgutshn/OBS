import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { feesApi } from '../../api/endpoints.js';
import { extractErrorMessage } from '../../api/client.js';
import { Card, CardBody } from '../../components/ui/Card.jsx';
import { Table } from '../../components/ui/Table.jsx';
import { Badge } from '../../components/ui/Badge.jsx';
import { formatCurrency, formatDate } from '../../lib/utils.js';

const STATUS_TONES = {
  PENDING: 'warning',
  PARTIAL: 'info',
  PAID: 'success',
  OVERDUE: 'danger',
  WAIVED: 'default',
};

export function StudentFees() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    feesApi.mine()
      .then(setData)
      .catch((err) => toast.error(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  const totalOutstanding = data.reduce((s, f) => s + Number(f.outstanding || 0), 0);
  const totalPaid = data.reduce((s, f) => s + Number(f.paidAmount || 0), 0);

  const columns = [
    { key: 'semester', header: 'Dönem' },
    { key: 'amount', header: 'Tutar', render: (f) => formatCurrency(f.amount) },
    { key: 'paidAmount', header: 'Ödenen', render: (f) => formatCurrency(f.paidAmount) },
    { key: 'outstanding', header: 'Kalan', render: (f) => formatCurrency(f.outstanding) },
    { key: 'dueDate', header: 'Son Ödeme', render: (f) => formatDate(f.dueDate) },
    { key: 'status', header: 'Durum', render: (f) =>
      <Badge tone={STATUS_TONES[f.status] || 'default'}>{f.status}</Badge> },
    { key: 'description', header: 'Açıklama', render: (f) => f.description || '-' },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Katkı Payı</h1>
        <p className="text-sm text-slate-500">Mali yükümlülükleriniz</p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <Card>
          <div className="p-5">
            <div className="text-sm text-slate-500">Toplam ödenen</div>
            <div className="mt-1 text-2xl font-bold text-emerald-700">{formatCurrency(totalPaid)}</div>
          </div>
        </Card>
        <Card>
          <div className="p-5">
            <div className="text-sm text-slate-500">Kalan borç</div>
            <div className="mt-1 text-2xl font-bold text-amber-700">{formatCurrency(totalOutstanding)}</div>
          </div>
        </Card>
      </div>

      <Card>
        <CardBody>
          {loading ? <div className="text-slate-500">Yükleniyor...</div>
            : <Table columns={columns} data={data} rowKey={(r) => r.id}
                emptyMessage="Tahakkuk eden katkı payı yok" />}
        </CardBody>
      </Card>
    </div>
  );
}
