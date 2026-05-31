import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Printer } from 'lucide-react';
import { reportsApi } from '../../api/endpoints.js';
import { extractErrorMessage } from '../../api/client.js';
import { Button } from '../../components/ui/Button.jsx';
import { TranscriptView } from '../../components/TranscriptView.jsx';

export function AdminTranscript() {
  const { studentId } = useParams();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    reportsApi.transcript(studentId)
      .then(setData)
      .catch((err) => toast.error(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [studentId]);

  if (loading) return <div className="text-slate-500">Yükleniyor...</div>;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between print:hidden">
        <h1 className="text-2xl font-bold text-slate-900">Transkript</h1>
        <Button variant="secondary" onClick={() => window.print()}>
          <Printer size={14} /> Yazdır
        </Button>
      </div>
      <TranscriptView data={data} />
    </div>
  );
}
