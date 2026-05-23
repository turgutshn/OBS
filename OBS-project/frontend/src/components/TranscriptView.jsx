import React from 'react';
import { Card, CardBody, CardHeader } from './ui/Card.jsx';
import { Badge } from './ui/Badge.jsx';

const STATUS_TONES = {
  ENROLLED: 'info',
  COMPLETED: 'success',
  DROPPED: 'default',
  FAILED: 'danger',
};

export function TranscriptView({ data }) {
  if (!data) return null;
  return (
    <div className="space-y-6 print:bg-white">
      <Card>
        <CardBody>
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <h2 className="text-xl font-bold text-slate-900">{data.studentName}</h2>
              <p className="text-sm text-slate-500">
                {data.studentNumber} · {data.department || '—'} · {data.enrollmentYear ? `Kayıt: ${data.enrollmentYear}` : ''}
              </p>
            </div>
            <div className="grid grid-cols-3 gap-6 text-center">
              <div>
                <div className="text-xs uppercase text-slate-500">GANO</div>
                <div className="text-2xl font-bold text-brand-700">{data.cumulativeGpa ?? '-'}</div>
              </div>
              <div>
                <div className="text-xs uppercase text-slate-500">Toplam Kredi</div>
                <div className="text-2xl font-bold text-slate-900">{data.totalCredits}</div>
              </div>
              <div>
                <div className="text-xs uppercase text-slate-500">Tamamlanan</div>
                <div className="text-2xl font-bold text-emerald-700">{data.completedCredits}</div>
              </div>
            </div>
          </div>
        </CardBody>
      </Card>

      {(data.semesters ?? []).map((sem) => (
        <Card key={sem.semester}>
          <CardHeader
            title={`Dönem: ${sem.semester}`}
            subtitle={`Dönem GNO: ${sem.semesterGpa ?? '-'} · ${sem.semesterCredits} kredi`}
          />
          <CardBody>
            <table className="min-w-full divide-y divide-slate-200 text-sm">
              <thead>
                <tr className="text-left text-xs uppercase text-slate-500">
                  <th className="py-2">Kod</th>
                  <th className="py-2">Ders</th>
                  <th className="py-2">Kredi</th>
                  <th className="py-2">Vize</th>
                  <th className="py-2">Final</th>
                  <th className="py-2">Harf</th>
                  <th className="py-2">Katsayı</th>
                  <th className="py-2">Durum</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {sem.enrollments.map((e) => (
                  <tr key={e.id}>
                    <td className="py-2 font-mono text-slate-700">{e.courseCode}</td>
                    <td className="py-2">{e.courseName}</td>
                    <td className="py-2">{e.courseCredits}</td>
                    <td className="py-2">{e.midtermGrade ?? '-'}</td>
                    <td className="py-2">{e.finalGrade ?? '-'}</td>
                    <td className="py-2 font-semibold">{e.letterGrade ?? '-'}</td>
                    <td className="py-2">{e.gradePoint ?? '-'}</td>
                    <td className="py-2">
                      <Badge tone={STATUS_TONES[e.status] || 'default'}>{e.status}</Badge>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </CardBody>
        </Card>
      ))}
    </div>
  );
}
