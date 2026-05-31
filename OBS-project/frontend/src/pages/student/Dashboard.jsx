import React, { useEffect, useState } from 'react';
import { BookOpen, Wallet, GraduationCap } from 'lucide-react';
import toast from 'react-hot-toast';
import { enrollmentsApi, feesApi, reportsApi } from '../../api/endpoints.js';
import { extractErrorMessage } from '../../api/client.js';
import { Card, CardBody, CardHeader } from '../../components/ui/Card.jsx';
import { Badge } from '../../components/ui/Badge.jsx';
import { useAuth } from '../../auth/AuthContext.jsx';
import { formatCurrency } from '../../lib/utils.js';

export function StudentDashboard() {
  const { user } = useAuth();
  const [enrollments, setEnrollments] = useState([]);
  const [fees, setFees] = useState([]);
  const [transcript, setTranscript] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      enrollmentsApi.mine().catch(() => []),
      feesApi.mine().catch(() => []),
      reportsApi.myTranscript().catch(() => null),
    ])
      .then(([e, f, t]) => { setEnrollments(e); setFees(f); setTranscript(t); })
      .catch((err) => toast.error(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="text-slate-500">Yükleniyor...</div>;

  const activeCount = enrollments.filter((e) => e.status === 'ENROLLED').length;
  const outstanding = fees.reduce((sum, f) => sum + Number(f.outstanding || 0), 0);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Hoş geldiniz, {user?.fullName}</h1>
        <p className="text-sm text-slate-500">Akademik durumunuza ve katkı paylarınıza buradan ulaşın.</p>
      </div>

      <div className="grid gap-4 sm:grid-cols-3">
        <Card>
          <div className="flex items-center gap-4 p-5">
            <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-brand-100 text-brand-700">
              <BookOpen size={22} />
            </div>
            <div>
              <div className="text-sm text-slate-500">Aktif ders</div>
              <div className="text-xl font-semibold">{activeCount}</div>
            </div>
          </div>
        </Card>
        <Card>
          <div className="flex items-center gap-4 p-5">
            <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-emerald-100 text-emerald-700">
              <GraduationCap size={22} />
            </div>
            <div>
              <div className="text-sm text-slate-500">GANO</div>
              <div className="text-xl font-semibold">{transcript?.cumulativeGpa ?? '-'}</div>
            </div>
          </div>
        </Card>
        <Card>
          <div className="flex items-center gap-4 p-5">
            <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-amber-100 text-amber-700">
              <Wallet size={22} />
            </div>
            <div>
              <div className="text-sm text-slate-500">Bekleyen katkı payı</div>
              <div className="text-xl font-semibold">{formatCurrency(outstanding)}</div>
            </div>
          </div>
        </Card>
      </div>

      <Card>
        <CardHeader title="Bu Dönem Derslerim" />
        <CardBody>
          {enrollments.filter((e) => e.status === 'ENROLLED').length === 0 ? (
            <p className="text-sm text-slate-500">Aktif ders kaydınız yok</p>
          ) : (
            <ul className="divide-y divide-slate-100">
              {enrollments.filter((e) => e.status === 'ENROLLED').map((e) => (
                <li key={e.id} className="flex items-center justify-between py-3">
                  <div>
                    <div className="font-medium text-slate-900">{e.courseCode} - {e.courseName}</div>
                    <div className="text-xs text-slate-500">
                      {e.teacherName || 'Öğretmen atanmadı'} · {e.semester} · {e.courseCredits} kredi
                    </div>
                  </div>
                  <Badge tone="info">{e.status}</Badge>
                </li>
              ))}
            </ul>
          )}
        </CardBody>
      </Card>
    </div>
  );
}
