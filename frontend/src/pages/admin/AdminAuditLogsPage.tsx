import { useState, useEffect } from 'react';
import { Button, Card, CardContent, Select, Badge, EmptyState, Skeleton } from '@/components/ui';
import { adminService, type AuditLog } from '@/services/admin.service';

const actionOptions = [
  { value: '', label: 'All Actions' },
  { value: 'USER_ENABLED', label: 'User Enabled' },
  { value: 'USER_DISABLED', label: 'User Disabled' },
  { value: 'ROLE_CHANGED', label: 'Role Changed' },
  { value: 'JOB_STATUS_CHANGED', label: 'Job Status Changed' },
];

const entityTypeOptions = [
  { value: '', label: 'All Entities' },
  { value: 'USER', label: 'User' },
  { value: 'JOB', label: 'Job' },
  { value: 'APPLICATION', label: 'Application' },
];

const actionBadgeColors: Record<string, 'success' | 'warning' | 'error' | 'info' | 'default'> = {
  USER_ENABLED: 'success',
  USER_DISABLED: 'error',
  ROLE_CHANGED: 'warning',
  JOB_STATUS_CHANGED: 'info',
};

export default function AdminAuditLogsPage() {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionFilter, setActionFilter] = useState('');
  const [entityFilter, setEntityFilter] = useState('');
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [totalElements, setTotalElements] = useState(0);

  const fetchLogs = async (pageNum: number = 0, reset: boolean = false) => {
    try {
      setLoading(true);
      setError(null);
      const response = await adminService.getAuditLogs({
        action: actionFilter || undefined,
        entityType: entityFilter || undefined,
        page: pageNum,
        size: 20,
      });
      if (response.success && response.data) {
        if (reset || pageNum === 0) {
          setLogs(response.data.content);
        } else {
          setLogs(prev => [...prev, ...response.data!.content]);
        }
        setHasMore(!response.data.last);
        setTotalElements(response.data.totalElements);
      }
    } catch (err) {
      setError('Failed to load audit logs');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs(0, true);
  }, [actionFilter, entityFilter]);

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-heading-lg text-neutral-900">Audit Logs</h1>
          <p className="text-body-sm text-neutral-500 mt-1">{totalElements} total entries</p>
        </div>
      </div>

      {error && (
        <div className="rounded-lg bg-error-50 p-4 text-body-sm text-error-700">{error}</div>
      )}

      <Card>
        <CardContent className="p-4">
          <div className="flex flex-col sm:flex-row gap-4">
            <Select
              options={actionOptions}
              value={actionFilter}
              onChange={e => setActionFilter(e.target.value)}
              className="w-full sm:w-48"
            />
            <Select
              options={entityTypeOptions}
              value={entityFilter}
              onChange={e => setEntityFilter(e.target.value)}
              className="w-full sm:w-40"
            />
          </div>
        </CardContent>
      </Card>

      {loading && logs.length === 0 ? (
        <div className="space-y-4">
          {[1, 2, 3, 4, 5].map(i => (
            <Card key={i}>
              <CardContent className="p-4">
                <div className="flex items-center gap-4">
                  <Skeleton className="h-10 w-10 rounded-full" />
                  <div className="flex-1 space-y-2">
                    <Skeleton className="h-5 w-48" />
                    <Skeleton className="h-4 w-64" />
                  </div>
                  <Skeleton className="h-4 w-32" />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : logs.length === 0 ? (
        <Card>
          <CardContent className="p-12">
            <EmptyState
              icon={
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                  <path d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              }
              title="No audit logs found"
              description="Try adjusting your filters."
            />
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {logs.map(log => (
            <Card key={log.id}>
              <CardContent className="p-4">
                <div className="flex items-start gap-4">
                  <div className="flex-shrink-0 w-10 h-10 rounded-full bg-neutral-100 flex items-center justify-center">
                    <span className="text-body-sm font-medium text-neutral-600">
                      {(log.actorName || '').split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)}
                    </span>
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <p className="text-body-sm font-medium text-neutral-900">{log.actorName}</p>
                      <Badge variant={actionBadgeColors[log.action] || 'default'} size="sm">
                        {log.action.replace(/_/g, ' ')}
                      </Badge>
                      <span className="text-caption text-neutral-400">{log.entityType} #{log.entityId}</span>
                    </div>
                    {log.description && (
                      <p className="text-body-sm text-neutral-600">{log.description}</p>
                    )}
                  </div>
                  <div className="flex-shrink-0 text-caption text-neutral-400">
                    {formatDate(log.createdAt)}
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}

          {hasMore && (
            <div className="flex justify-center pt-4">
              <Button
                variant="outline"
                onClick={() => {
                  const nextPage = page + 1;
                  setPage(nextPage);
                  fetchLogs(nextPage);
                }}
                loading={loading}
              >
                Load More
              </Button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
