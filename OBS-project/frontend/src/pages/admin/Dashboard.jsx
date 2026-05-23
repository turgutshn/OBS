import React, { useEffect, useState } from 'react';
import { GraduationCap, Users, BookOpen, Wallet, AlertTriangle } from 'lucide-react';
import toast from 'react-hot-toast';
import { reportsApi } from '../../api/endpoints.js';
import { extractErrorMessage } from '../../api/client.js';
import { Card, CardBody, CardHeader } from '../../components/ui/Card.jsx';
import { formatCurrency } from '../../lib/utils.js';

function Metric({ icon: Icon, label, value, tone = 'brand' }) {
  const tones = {
    brand: 'bg-brand-100 text-brand-700',
    emerald: 'bg-emerald-100 text-emerald-700',
    amber: 'bg-amber-100 text-amber-700',
    red: 'bg-red-100 text-red-700',
  };
  return (
    <div className="card">
      <div className="flex items-center gap-4 p-5">
        <div className={`flex h-12 w-12 items-center justify-center rounded-lg ${tones[tone]}`}>
          <Icon size={22} />
        </div>
        <div>
          <div className="text-sm text-slate-500">{label}</div>
          <div className="text-xl font-semibold text-slate-900">{value}</div>
        </div>
      </div>
    </div>
  );
}

export function AdminDashboard() {
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
        <h1 className="text-2xl font-bold text-slate-900">Genel Bakış</h1>
        <p className="text-sm text-slate-500">Sistemdeki güncel istatistikler</p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Metric icon={GraduationCap} label="Öğrenci sayısı" value={summary.totalStudents} tone="brand" />
        <Metric icon={Users} label="Öğretmen sayısı" value={summary.totalTeachers} tone="emerald" />
        <Metric icon={BookOpen} label="Ders sayısı" value={summary.totalCourses} tone="amber" />
        <Metric icon={Wallet} label="Toplam tahakkuk" value={formatCurrency(summary.totalAssessedFees)} tone="red" />
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader title="Bölümlere göre öğrenci dağılımı" />
          <CardBody>
            {Object.entries(summary.studentsByDepartment).length === 0 ? (
              <p className="text-sm text-slate-500">Veri yok</p>
            ) : (
              <ul className="space-y-2">
                {Object.entries(summary.studentsByDepartment).map(([k, v]) => (
                  <li key={k} className="flex items-center justify-between text-sm">
                    <span className="text-slate-700">{k}</span>
                    <span className="font-semibold text-slate-900">{v}</span>
                  </li>
                ))}
              </ul>
            )}
          </CardBody>
        </Card>

        <Card>
          <CardHeader title="Katkı payı durumu" />
          <CardBody>
            <dl className="space-y-3 text-sm">
              <div className="flex justify-between">
                <dt className="text-slate-600">Tahakkuk eden</dt>
                <dd className="font-semibold">{formatCurrency(summary.totalAssessedFees)}</dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-slate-600">Tahsil edilen</dt>
                <dd className="font-semibold text-emerald-700">{formatCurrency(summary.totalCollectedFees)}</dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-slate-600">Bekleyen</dt>
                <dd className="font-semibold text-amber-700">{formatCurrency(summary.totalOutstandingFees)}</dd>
              </div>
            </dl>
            {(summary.feesByStatus?.OVERDUE ?? 0) > 0 && (
              <div className="mt-4 flex items-center gap-2 rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-800">
                <AlertTriangle size={16} />
                {summary.feesByStatus.OVERDUE} adet geciken katkı payı var
              </div>
            )}
          </CardBody>
        </Card>
      </div>
    </div>
  );
}
