import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { reportsApi } from '../../api/endpoints.js';
import { extractErrorMessage } from '../../api/client.js';
import { Card, CardBody, CardHeader } from '../../components/ui/Card.jsx';
import { formatCurrency } from '../../lib/utils.js';

function KeyValueList({ data }) {
  const entries = Object.entries(data || {});
  if (!entries.length) return <p className="text-sm text-slate-500">Veri yok</p>;
  return (
    <ul className="space-y-1.5 text-sm">
      {entries.map(([k, v]) => (
        <li key={k} className="flex items-center justify-between">
          <span className="text-slate-600">{k}</span>
          <span className="font-semibold text-slate-900">{v}</span>
        </li>
      ))}
    </ul>
  );
}

export function AdminReports() {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    reportsApi.summary()
      .then(setSummary)
      .catch((err) => toast.error(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="text-slate-500">Yükleniyor...</div>;
  if (!summary) return <div className="text-slate-500">Veri yok</div>;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Raporlar</h1>
        <p className="text-sm text-slate-500">Sistem genel raporu</p>
      </div>

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        <Card>
          <CardHeader title="Toplamlar" />
          <CardBody className="space-y-2 text-sm">
            <div className="flex justify-between"><span>Öğrenci</span><b>{summary.totalStudents}</b></div>
            <div className="flex justify-between"><span>Öğretmen</span><b>{summary.totalTeachers}</b></div>
            <div className="flex justify-between"><span>Ders</span><b>{summary.totalCourses}</b></div>
            <div className="flex justify-between"><span>Kayıt</span><b>{summary.totalEnrollments}</b></div>
          </CardBody>
        </Card>

        <Card>
          <CardHeader title="Mali Durum" />
          <CardBody className="space-y-2 text-sm">
            <div className="flex justify-between"><span>Tahakkuk</span><b>{formatCurrency(summary.totalAssessedFees)}</b></div>
            <div className="flex justify-between"><span>Tahsilat</span><b className="text-emerald-700">{formatCurrency(summary.totalCollectedFees)}</b></div>
            <div className="flex justify-between"><span>Kalan</span><b className="text-amber-700">{formatCurrency(summary.totalOutstandingFees)}</b></div>
          </CardBody>
        </Card>

        <Card>
          <CardHeader title="Katkı Payı Durumları" />
          <CardBody><KeyValueList data={summary.feesByStatus} /></CardBody>
        </Card>

        <Card>
          <CardHeader title="Öğrenci · Bölüm Dağılımı" />
          <CardBody><KeyValueList data={summary.studentsByDepartment} /></CardBody>
        </Card>

        <Card>
          <CardHeader title="Ders · Bölüm Dağılımı" />
          <CardBody><KeyValueList data={summary.coursesByDepartment} /></CardBody>
        </Card>
      </div>
    </div>
  );
}
