import React, { useCallback, useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { reportsApi } from '../../api/endpoints.js';
import { extractErrorMessage } from '../../api/client.js';
import { Card, CardBody } from '../../components/ui/Card.jsx';
import { Input } from '../../components/ui/Input.jsx';
import { Button } from '../../components/ui/Button.jsx';
import { Table } from '../../components/ui/Table.jsx';
import { Badge } from '../../components/ui/Badge.jsx';
import { formatDateTime } from '../../lib/utils.js';

export function AdminAuditLogs() {
  const [page, setPage] = useState({ content: [], totalPages: 0, page: 0, totalElements: 0 });
  const [filters, setFilters] = useState({ actor: '', action: '' });
  const [loading, setLoading] = useState(false);

  const load = useCallback(async (p = 0) => {
    setLoading(true);
    try {
      const data = await reportsApi.auditLogs({ ...filters, page: p, size: 50 });
      setPage(data);
    } catch (err) {
      toast.error(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => { load(0); /* eslint-disable-next-line */ }, []);

  const columns = [
    { key: 'occurredAt', header: 'Zaman', render: (a) => formatDateTime(a.occurredAt) },
    { key: 'actorUsername', header: 'Kullanıcı', render: (a) => a.actorUsername || '-' },
    { key: 'actorRole', header: 'Rol' },
    { key: 'action', header: 'İşlem' },
    { key: 'entityType', header: 'Varlık', render: (a) => a.entityType ? `${a.entityType}:${a.entityId || ''}` : '-' },
    { key: 'status', header: 'Sonuç', render: (a) =>
      <Badge tone={a.status === 'SUCCESS' ? 'success' : 'danger'}>{a.status}</Badge> },
    { key: 'ipAddress', header: 'IP', render: (a) => a.ipAddress || '-' },
    { key: 'details', header: 'Detay', render: (a) => <span className="text-xs text-slate-500">{a.details || '-'}</span> },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Denetim Kayıtları</h1>
        <p className="text-sm text-slate-500">Tüm güvenlik ve iş işlemleri</p>
      </div>

      <Card>
        <CardBody className="space-y-4">
          <form onSubmit={(e) => { e.preventDefault(); load(0); }} className="flex flex-wrap gap-3">
            <Input placeholder="Kullanıcı" value={filters.actor}
              onChange={(e) => setFilters((s) => ({ ...s, actor: e.target.value }))} className="max-w-xs" />
            <Input placeholder="İşlem (LOGIN, STUDENT_CREATE, ...)" value={filters.action}
              onChange={(e) => setFilters((s) => ({ ...s, action: e.target.value }))} className="max-w-xs" />
            <Button variant="secondary" type="submit">Filtrele</Button>
          </form>
          {loading ? <div className="text-slate-500">Yükleniyor...</div>
            : <Table columns={columns} data={page.content} rowKey={(r) => r.id} />}
          {page.totalPages > 1 && (
            <div className="flex items-center justify-between text-sm">
              <span className="text-slate-500">Sayfa {page.page + 1} / {page.totalPages}</span>
              <div className="flex gap-2">
                <Button variant="secondary" disabled={page.page === 0} onClick={() => load(page.page - 1)}>Önceki</Button>
                <Button variant="secondary" disabled={page.page + 1 >= page.totalPages} onClick={() => load(page.page + 1)}>Sonraki</Button>
              </div>
            </div>
          )}
        </CardBody>
      </Card>
    </div>
  );
}
