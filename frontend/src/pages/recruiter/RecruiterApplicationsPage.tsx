import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Card, CardContent, Badge, Button, Input, Skeleton, EmptyState, Select } from '@/components/ui';
import { recruiterApplicationService, type ApplicationSummary, type ApplicationStats } from '@/services/recruiterApplication.service';

const statusVariant: Record<string, 'info' | 'warning' | 'success' | 'error' | 'primary' | 'default'> = {
  APPLIED: 'info',
  UNDER_REVIEW: 'warning',
  SHORTLISTED: 'success',
  INTERVIEW: 'info',
  REJECTED: 'error',
  HIRED: 'primary',
  WITHDRAWN: 'default',
};

const statusLabels: Record<string, string> = {
  APPLIED: 'Applied',
  UNDER_REVIEW: 'Under Review',
  SHORTLISTED: 'Shortlisted',
  INTERVIEW: 'Interview',
  REJECTED: 'Rejected',
  HIRED: 'Hired',
  WITHDRAWN: 'Withdrawn',
};

const statusOptions = [
  { value: '', label: 'All Statuses' },
  { value: 'APPLIED', label: 'Applied' },
  { value: 'UNDER_REVIEW', label: 'Under Review' },
  { value: 'SHORTLISTED', label: 'Shortlisted' },
  { value: 'INTERVIEW', label: 'Interview' },
  { value: 'REJECTED', label: 'Rejected' },
  { value: 'HIRED', label: 'Hired' },
  { value: 'WITHDRAWN', label: 'Withdrawn' },
];

const sortOptions = [
  { value: 'appliedAt,desc', label: 'Newest First' },
  { value: 'appliedAt,asc', label: 'Oldest First' },
  { value: 'candidateName,asc', label: 'Candidate A-Z' },
];

function formatDate(dateStr: string): string {
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return '-';
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function StatsCards({ stats, loading }: { stats: ApplicationStats | null; loading: boolean }) {
  const cards = stats
    ? [
        { label: 'Total Applications', value: stats.totalApplications },
        { label: 'Applied', value: stats.appliedCount },
        { label: 'Under Review', value: stats.underReviewCount },
        { label: 'Shortlisted', value: stats.shortlistedCount },
        { label: 'Rejected', value: stats.rejectedCount },
        { label: 'Hired', value: stats.hiredCount },
      ]
    : [
        { label: 'Total Applications', value: 0 },
        { label: 'Applied', value: 0 },
        { label: 'Under Review', value: 0 },
        { label: 'Shortlisted', value: 0 },
        { label: 'Rejected', value: 0 },
        { label: 'Hired', value: 0 },
      ];

  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
      {cards.map((card) => (
        <Card key={card.label} padding="md">
          <p className="text-body-sm text-neutral-500">{card.label}</p>
          {loading ? (
            <Skeleton className="h-8 w-12 mt-1" />
          ) : (
            <p className="text-display-sm text-neutral-900 mt-1">{card.value}</p>
          )}
        </Card>
      ))}
    </div>
  );
}

function ApplicationRowSkeleton() {
  return (
    <tr className="border-b border-neutral-100 last:border-0">
      <td className="px-4 py-3"><Skeleton className="h-4 w-32" /></td>
      <td className="px-4 py-3"><Skeleton className="h-4 w-40" /></td>
      <td className="px-4 py-3"><Skeleton className="h-5 w-24 rounded-full" /></td>
      <td className="px-4 py-3"><Skeleton className="h-4 w-24" /></td>
    </tr>
  );
}

function ApplicationCardSkeleton() {
  return (
    <div className="rounded-lg border border-neutral-200 p-4">
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <Skeleton className="h-4 w-32 mb-2" />
          <Skeleton className="h-3 w-40 mb-2" />
          <Skeleton className="h-5 w-24 rounded-full" />
        </div>
        <Skeleton className="h-4 w-20" />
      </div>
    </div>
  );
}

export default function RecruiterApplicationsPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const [applications, setApplications] = useState<ApplicationSummary[]>([]);
  const [stats, setStats] = useState<ApplicationStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [statsLoading, setStatsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const page = Number(searchParams.get('page')) || 0;
  const search = searchParams.get('q') || '';
  const statusFilter = searchParams.get('status') || '';
  const sort = searchParams.get('sort') || 'appliedAt,desc';

  const fetchApplications = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const params: Record<string, string | number> = { page, size: 12, sort };
      if (search) params.q = search;
      if (statusFilter) params.status = statusFilter;
      const data = await recruiterApplicationService.getApplications(params);
      setApplications(data.content);
      setTotalElements(data.totalElements);
      setTotalPages(data.totalPages);
    } catch {
      setError('Failed to load applications.');
    } finally {
      setLoading(false);
    }
  }, [page, search, statusFilter, sort]);

  const fetchStats = useCallback(async () => {
    try {
      setStatsLoading(true);
      const data = await recruiterApplicationService.getStats();
      setStats(data);
    } catch {
      // fallback
    } finally {
      setStatsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchApplications();
  }, [fetchApplications]);

  useEffect(() => {
    fetchStats();
  }, [fetchStats]);

  const updateParam = (key: string, value: string) => {
    const next = new URLSearchParams(searchParams);
    if (value) {
      next.set(key, value);
    } else {
      next.delete(key);
    }
    if (key !== 'page') next.delete('page');
    setSearchParams(next);
  };

  const handleSearch = (e: React.ChangeEvent<HTMLInputElement>) => {
    updateParam('q', e.target.value);
  };

  const handleStatusChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    updateParam('status', e.target.value);
  };

  const handleSortChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    updateParam('sort', e.target.value);
  };

  const goToPage = (newPage: number) => {
    updateParam('page', String(newPage));
  };

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-heading-lg text-neutral-900">Applications</h2>
          <p className="text-body-md text-neutral-500 mt-1">Review and manage candidate applications</p>
        </div>
      </div>

      <StatsCards stats={stats} loading={statsLoading} />

      <Card>
        <CardContent className="p-5">
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="flex-1">
              <Input
                placeholder="Search by candidate name or email..."
                value={search}
                onChange={handleSearch}
              />
            </div>
            <div className="w-full sm:w-44">
              <Select
                options={statusOptions}
                value={statusFilter}
                onChange={handleStatusChange}
              />
            </div>
            <div className="w-full sm:w-40">
              <Select
                options={sortOptions}
                value={sort}
                onChange={handleSortChange}
              />
            </div>
          </div>
        </CardContent>
      </Card>

      {loading ? (
        <>
          <div className="hidden md:block">
            <Card padding="none">
              <table className="w-full text-left">
                <thead>
                  <tr className="border-b border-neutral-100">
                    <th className="px-4 py-3 text-overline">Candidate</th>
                    <th className="px-4 py-3 text-overline">Job</th>
                    <th className="px-4 py-3 text-overline">Status</th>
                    <th className="px-4 py-3 text-overline">Applied</th>
                  </tr>
                </thead>
                <tbody>
                  {Array.from({ length: 5 }).map((_, i) => (
                    <ApplicationRowSkeleton key={i} />
                  ))}
                </tbody>
              </table>
            </Card>
          </div>
          <div className="md:hidden space-y-3">
            {Array.from({ length: 4 }).map((_, i) => (
              <ApplicationCardSkeleton key={i} />
            ))}
          </div>
        </>
      ) : error ? (
        <Card>
          <CardContent className="p-8 text-center">
            <p className="text-body-md text-error-600 mb-4">{error}</p>
            <Button variant="outline" onClick={fetchApplications}>Try Again</Button>
          </CardContent>
        </Card>
      ) : applications.length === 0 ? (
        <Card>
          <CardContent className="p-8">
            <EmptyState
              icon={
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M9 12h3.75M9 15h3.75M9 18h3.75m3 .75H18a2.25 2.25 0 002.25-2.25V6.108c0-1.135-.845-2.098-1.976-2.192a48.424 48.424 0 00-1.123-.08m-5.801 0c-.065.21-.1.433-.1.664 0 .414.336.75.75.75h4.5a.75.75 0 00.75-.75 2.25 2.25 0 00-.1-.664m-5.8 0A2.251 2.251 0 0113.5 2.25H15c1.012 0 1.867.668 2.15 1.586m-5.8 0c-.376.023-.75.05-1.124.08C9.095 4.01 8.25 4.973 8.25 6.108V8.25" />
                </svg>
              }
              title="No applications found"
              description="No candidates have applied to your jobs yet, or no results match your filters."
            />
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="text-body-sm text-neutral-500">
            {totalElements} application{totalElements !== 1 ? 's' : ''}
          </div>

          {/* Desktop table */}
          <div className="hidden md:block">
            <Card padding="none">
              <div className="overflow-x-auto">
                <table className="w-full text-left">
                  <thead>
                    <tr className="border-b border-neutral-100">
                      <th className="px-4 py-3 text-overline">Candidate</th>
                      <th className="px-4 py-3 text-overline">Job</th>
                      <th className="px-4 py-3 text-overline">Status</th>
                      <th className="px-4 py-3 text-overline">Applied</th>
                    </tr>
                  </thead>
                  <tbody>
                    {applications.map((app) => (
                      <tr
                        key={app.id}
                        className="border-b border-neutral-50 last:border-0 hover:bg-neutral-50/50 transition-colors cursor-pointer"
                        onClick={() => navigate(`/recruiter/applications/${app.id}`)}
                      >
                        <td className="px-4 py-3">
                          <span className="text-body-sm font-medium text-neutral-900">{app.candidateName}</span>
                          <span className="text-body-sm text-neutral-500 block">{app.candidateEmail}</span>
                        </td>
                        <td className="px-4 py-3">
                          <span className="text-body-sm text-neutral-600">{app.jobTitle}</span>
                        </td>
                        <td className="px-4 py-3">
                          <Badge variant={statusVariant[app.status] || 'default'} size="sm" dot>
                            {statusLabels[app.status] || app.status}
                          </Badge>
                        </td>
                        <td className="px-4 py-3">
                          <span className="text-body-sm text-neutral-500">{formatDate(app.appliedAt)}</span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </Card>
          </div>

          {/* Mobile cards */}
          <div className="md:hidden space-y-3">
            {applications.map((app) => (
              <Card
                key={app.id}
                padding="md"
                className="hover:border-primary-200 hover:shadow-sm transition-all cursor-pointer"
                onClick={() => navigate(`/recruiter/applications/${app.id}`)}
              >
                <div className="flex items-start justify-between">
                  <div className="min-w-0 flex-1">
                    <p className="text-body-md font-medium text-neutral-900">{app.candidateName}</p>
                    <p className="text-body-sm text-neutral-500 mt-0.5">{app.jobTitle}</p>
                  </div>
                  <Badge variant={statusVariant[app.status] || 'default'} size="sm" dot>
                    {statusLabels[app.status] || app.status}
                  </Badge>
                </div>
                <div className="mt-2">
                  <span className="text-body-sm text-neutral-500">{formatDate(app.appliedAt)}</span>
                </div>
              </Card>
            ))}
          </div>

          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 pt-4">
              <Button
                variant="outline"
                size="sm"
                disabled={page === 0}
                onClick={() => goToPage(page - 1)}
              >
                Previous
              </Button>
              <span className="text-body-sm text-neutral-600 px-3">
                Page {page + 1} of {totalPages}
              </span>
              <Button
                variant="outline"
                size="sm"
                disabled={page >= totalPages - 1}
                onClick={() => goToPage(page + 1)}
              >
                Next
              </Button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
