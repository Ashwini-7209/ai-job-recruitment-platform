import { useState, useEffect } from 'react';
import { Button, Card, CardContent, Input, Select, Badge, EmptyState, Skeleton } from '@/components/ui';
import { adminService, type AdminApplication } from '@/services/admin.service';

const statusOptions = [
  { value: '', label: 'All Status' },
  { value: 'APPLIED', label: 'Applied' },
  { value: 'UNDER_REVIEW', label: 'Under Review' },
  { value: 'SHORTLISTED', label: 'Shortlisted' },
  { value: 'INTERVIEW', label: 'Interview' },
  { value: 'REJECTED', label: 'Rejected' },
  { value: 'HIRED', label: 'Hired' },
  { value: 'WITHDRAWN', label: 'Withdrawn' },
];

const statusBadgeColors: Record<string, 'default' | 'primary' | 'success' | 'warning' | 'error'> = {
  APPLIED: 'default',
  UNDER_REVIEW: 'primary',
  SHORTLISTED: 'success',
  INTERVIEW: 'primary',
  REJECTED: 'error',
  HIRED: 'success',
  WITHDRAWN: 'warning',
};

export default function AdminApplicationsPage() {
  const [applications, setApplications] = useState<AdminApplication[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [totalElements, setTotalElements] = useState(0);

  const fetchApplications = async (pageNum: number = 0, reset: boolean = false) => {
    try {
      setLoading(true);
      setError(null);
      const response = await adminService.getApplications({
        status: statusFilter || undefined,
        candidateName: search || undefined,
        page: pageNum,
        size: 20,
      });
      if (response.success && response.data) {
        if (reset || pageNum === 0) {
          setApplications(response.data.content ?? []);
        } else {
          setApplications(prev => [...prev, ...(response.data!.content ?? [])]);
        }
        setHasMore(!response.data.last);
        setTotalElements(response.data.totalElements);
      }
    } catch (err) {
      setError('Failed to load applications');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchApplications(0, true);
  }, [statusFilter, search]);

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-heading-lg text-neutral-900">Applications</h1>
          <p className="text-body-sm text-neutral-500 mt-1">{totalElements} total applications</p>
        </div>
      </div>

      {error && (
        <div className="rounded-lg bg-error-50 p-4 text-body-sm text-error-700">{error}</div>
      )}

      <Card>
        <CardContent className="p-4">
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1">
              <Input
                placeholder="Search by candidate name..."
                value={search}
                onChange={e => { setSearch(e.target.value); setPage(0); }}
                leftIcon={
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <circle cx="11" cy="11" r="8" />
                    <line x1="21" y1="21" x2="16.65" y2="16.65" />
                  </svg>
                }
              />
            </div>
            <Select
              options={statusOptions}
              value={statusFilter}
              onChange={e => { setStatusFilter(e.target.value); setPage(0); }}
              className="w-full sm:w-48"
            />
          </div>
        </CardContent>
      </Card>

      {loading && applications.length === 0 ? (
        <div className="space-y-4">
          {[1, 2, 3, 4, 5].map(i => (
            <Card key={i}>
              <CardContent className="p-4">
                <div className="flex items-center gap-4">
                  <div className="flex-1 space-y-2">
                    <Skeleton className="h-5 w-48" />
                    <Skeleton className="h-4 w-32" />
                  </div>
                  <Skeleton className="h-6 w-24" />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : applications.length === 0 ? (
        <Card>
          <CardContent className="p-12">
            <EmptyState
              icon={
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                  <path d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
                </svg>
              }
              title="No applications found"
              description="Try adjusting your filters."
            />
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {applications.map(app => (
            <Card key={app.id}>
              <CardContent className="p-4">
                <div className="flex items-center justify-between gap-4">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <h3 className="text-body-lg font-medium text-neutral-900 truncate">{app.jobTitle}</h3>
                      <Badge variant={statusBadgeColors[app.status]} size="sm">
                        {app.status.replace(/_/g, ' ')}
                      </Badge>
                    </div>
                    <div className="flex flex-wrap gap-4 text-body-sm text-neutral-500">
                      <span>Candidate: {app.candidateName}</span>
                      <span>Recruiter: {app.recruiterName}</span>
                    </div>
                    <div className="flex gap-4 text-caption text-neutral-400 mt-2">
                      <span>Applied {formatDate(app.appliedAt)}</span>
                    </div>
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
                  fetchApplications(nextPage);
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
