import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { enrollmentsApi } from '../../api/endpoints.js';
import { extractErrorMessage } from '../../api/client.js';
import { Card, CardBody } from '../../components/ui/Card.jsx';
import { Table } from '../../components/ui/Table.jsx';
import { Badge } from '../../components/ui/Badge.jsx';

const STATUS_TONES = {
  ENROLLED: 'info',
  COMPLETED: 'success',
  DROPPED: 'default',
  FAILED: 'danger',
};

export function StudentCourses() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    enrollmentsApi.mine()
      .then(setData)
      .catch((err) => toast.error(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  const columns = [
    { key: 'semester', header: 'Dönem' },
    { key: 'courseCode', header: 'Kod' },
    { key: 'courseName', header: 'Ders' },
    { key: 'courseCredits', header: 'Kredi' },
    { key: 'teacherName', header: 'Öğretmen', render: (e) => e.teacherName || '-' },
    { key: 'midtermGrade', header: 'Vize', render: (e) => e.midtermGrade ?? '-' },
    { key: 'finalGrade', header: 'Final', render: (e) => e.finalGrade ?? '-' },
    { key: 'letterGrade', header: 'Harf', render: (e) => e.letterGrade || '-' },
    { key: 'status', header: 'Durum', render: (e) =>
      <Badge tone={STATUS_TONES[e.status] || 'default'}>{e.status}</Badge> },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Derslerim</h1>
        <p className="text-sm text-slate-500">Aldığınız tüm derslerin listesi</p>
      </div>
      <Card>
        <CardBody>
          {loading ? <div className="text-slate-500">Yükleniyor...</div>
            : <Table columns={columns} data={data} rowKey={(r) => r.id} />}
        </CardBody>
      </Card>
    </div>
  );
}
