import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { Plus, Pencil } from 'lucide-react';
import { coursesApi, enrollmentsApi, studentsApi } from '../../api/endpoints.js';
import { extractErrorMessage } from '../../api/client.js';
import { Card, CardBody, CardHeader } from '../../components/ui/Card.jsx';
import { Button } from '../../components/ui/Button.jsx';
import { Input, Select } from '../../components/ui/Input.jsx';
import { Table } from '../../components/ui/Table.jsx';
import { Modal } from '../../components/ui/Modal.jsx';
import { Badge } from '../../components/ui/Badge.jsx';

const STATUS_TONES = {
  ENROLLED: 'info',
  COMPLETED: 'success',
  DROPPED: 'default',
  FAILED: 'danger',
};

export function AdminEnrollments() {
  const [students, setStudents] = useState([]);
  const [courses, setCourses] = useState([]);
  const [selectedCourse, setSelectedCourse] = useState('');
  const [enrollments, setEnrollments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [createOpen, setCreateOpen] = useState(false);
  const [form, setForm] = useState({ studentId: '', courseId: '', semester: '' });
  const [gradeModal, setGradeModal] = useState({ open: false, enrollment: null });
  const [grade, setGrade] = useState({ midtermGrade: '', finalGrade: '' });

  useEffect(() => {
    studentsApi.list({ size: 500, sort: 'lastName' }).then((d) => setStudents(d.content)).catch(() => {});
    coursesApi.list({ size: 500, sort: 'code' }).then((d) => setCourses(d.content)).catch(() => {});
  }, []);

  const loadEnrollments = async (courseId) => {
    if (!courseId) { setEnrollments([]); return; }
    setLoading(true);
    try {
      const data = await coursesApi.enrollments(courseId);
      setEnrollments(data);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const submitCreate = async (e) => {
    e.preventDefault();
    try {
      await enrollmentsApi.enroll({
        studentId: Number(form.studentId),
        courseId: Number(form.courseId),
        semester: form.semester,
      });
      toast.success('Öğrenci derse kaydedildi');
      setCreateOpen(false);
      setForm({ studentId: '', courseId: '', semester: '' });
      if (selectedCourse) loadEnrollments(selectedCourse);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    }
  };

  const openGrade = (en) => {
    setGrade({
      midtermGrade: en.midtermGrade ?? '',
      finalGrade: en.finalGrade ?? '',
    });
    setGradeModal({ open: true, enrollment: en });
  };

  const submitGrade = async (e) => {
    e.preventDefault();
    try {
      await enrollmentsApi.grade(gradeModal.enrollment.id, {
        midtermGrade: grade.midtermGrade === '' ? null : Number(grade.midtermGrade),
        finalGrade: grade.finalGrade === '' ? null : Number(grade.finalGrade),
      });
      toast.success('Not kaydedildi');
      setGradeModal({ open: false, enrollment: null });
      loadEnrollments(selectedCourse);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    }
  };

  const onDrop = async (en) => {
    if (!confirm('Kayıt iptal edilsin mi?')) return;
    try {
      await enrollmentsApi.drop(en.id);
      toast.success('Kayıt iptal edildi');
      loadEnrollments(selectedCourse);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    }
  };

  const columns = [
    { key: 'studentNumber', header: 'Öğrenci No' },
    { key: 'studentName', header: 'Öğrenci' },
    { key: 'semester', header: 'Dönem' },
    { key: 'midtermGrade', header: 'Vize', render: (e) => e.midtermGrade ?? '-' },
    { key: 'finalGrade', header: 'Final', render: (e) => e.finalGrade ?? '-' },
    { key: 'letterGrade', header: 'Harf', render: (e) => e.letterGrade || '-' },
    { key: 'status', header: 'Durum', render: (e) =>
      <Badge tone={STATUS_TONES[e.status] || 'default'}>{e.status}</Badge> },
    {
      key: 'actions', header: '', className: 'text-right',
      render: (e) => (
        <div className="flex justify-end gap-2">
          <button className="btn-ghost" onClick={() => openGrade(e)}><Pencil size={14} /> Not</button>
          <button className="btn-ghost text-red-600" onClick={() => onDrop(e)}>İptal</button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Ders Atama / Kayıt</h1>
          <p className="text-sm text-slate-500">Öğrencileri derslere kaydedin, notları girin</p>
        </div>
        <Button onClick={() => setCreateOpen(true)}><Plus size={16} /> Yeni Kayıt</Button>
      </div>

      <Card>
        <CardHeader title="Ders bazında kayıtlar" />
        <CardBody className="space-y-4">
          <Select
            label="Ders seçin"
            value={selectedCourse}
            onChange={(e) => { setSelectedCourse(e.target.value); loadEnrollments(e.target.value); }}
          >
            <option value="">— Ders seçin —</option>
            {courses.map((c) => (
              <option key={c.id} value={c.id}>{c.code} - {c.name}</option>
            ))}
          </Select>
          {loading ? <div className="text-slate-500">Yükleniyor...</div>
            : <Table columns={columns} data={enrollments} rowKey={(r) => r.id}
                emptyMessage={selectedCourse ? 'Bu derste kayıt yok' : 'Bir ders seçin'} />}
        </CardBody>
      </Card>

      <Modal open={createOpen} onClose={() => setCreateOpen(false)} title="Yeni Kayıt">
        <form id="enrollment-form" onSubmit={submitCreate} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Select label="Öğrenci *" required value={form.studentId}
            onChange={(e) => setForm((s) => ({ ...s, studentId: e.target.value }))}>
            <option value="">— Öğrenci seçin —</option>
            {students.map((s) => (
              <option key={s.id} value={s.id}>{s.studentNumber} - {s.firstName} {s.lastName}</option>
            ))}
          </Select>
          <Select label="Ders *" required value={form.courseId}
            onChange={(e) => setForm((s) => ({ ...s, courseId: e.target.value }))}>
            <option value="">— Ders seçin —</option>
            {courses.map((c) => (
              <option key={c.id} value={c.id}>{c.code} - {c.name}</option>
            ))}
          </Select>
          <div className="sm:col-span-2">
            <Input label="Dönem *" required placeholder="2025-Güz"
              value={form.semester} onChange={(e) => setForm((s) => ({ ...s, semester: e.target.value }))} />
          </div>
        </form>
        <div className="mt-5 flex justify-end gap-2">
          <Button variant="secondary" onClick={() => setCreateOpen(false)}>İptal</Button>
          <Button form="enrollment-form" type="submit">Kaydet</Button>
        </div>
      </Modal>

      <Modal open={gradeModal.open} onClose={() => setGradeModal({ open: false, enrollment: null })}
        title={`Not Girişi - ${gradeModal.enrollment?.studentName || ''}`}>
        <form id="grade-form" onSubmit={submitGrade} className="grid grid-cols-2 gap-4">
          <Input label="Vize (0-100)" type="number" min="0" max="100" step="0.01"
            value={grade.midtermGrade}
            onChange={(e) => setGrade((s) => ({ ...s, midtermGrade: e.target.value }))} />
          <Input label="Final (0-100)" type="number" min="0" max="100" step="0.01"
            value={grade.finalGrade}
            onChange={(e) => setGrade((s) => ({ ...s, finalGrade: e.target.value }))} />
          <p className="col-span-2 text-xs text-slate-500">
            Not hesaplaması: %40 Vize + %60 Final. Harf notu ve geçti/kaldı otomatik hesaplanır.
          </p>
        </form>
        <div className="mt-5 flex justify-end gap-2">
          <Button variant="secondary" onClick={() => setGradeModal({ open: false, enrollment: null })}>İptal</Button>
          <Button form="grade-form" type="submit">Kaydet</Button>
        </div>
      </Modal>
    </div>
  );
}
