import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, Badge, Button, Skeleton, EmptyState } from '@/components/ui';
import { recruiterJobService, type RecruiterJobSummary, type RecruiterJobStats } from '@/services/recruiterJob.service';

type StatusFilter = 'ALL' | 'DRAFT' | 'PUBLISHED' | 'CLOSED';

const statusVariant: Record<string, 'default' | 'success' | 'error'> = {
  DRAFT: 'default',
  PUBLISHED: 'success',
  CLOSED: 'error',
};

const statusLabels: Record<string, string> = {
  DRAFT: 'Draft',
  PUBLISHED: 'Published',
  CLOSED: 'Closed',
};

const employmentTypeLabels: Record<string, string> = {
  FULL_TIME: 'Full Time',
  PART_TIME: 'Part Time',
  CONTRACT: 'Contract',
  INTERNSHIP: 'Internship',
  FREELANCE: 'Freelance',
};

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function StatsCards({ stats, loading }: { stats: RecruiterJobStats | null; loading: boolean }) {
  const cards = stats
    ? [
        { label: 'Total Jobs', value: stats.totalJobs, color: 'text-neutral-900' },
        { label: 'Draft', value: stats.draftJobs, color: 'text-neutral-600' },
        { label: 'Published', value: stats.publishedJobs, color: 'text-success-600' },
        { label: 'Closed', value: stats.closedJobs, color: 'text-error-600' },
      ]
    : [
        { label: 'Total Jobs', value: 0, color: 'text-neutral-900' },
        { label: 'Draft', value: 0, color: 'text-neutral-600' },
        { label: 'Published', value: 0, color: 'text-success-600' },
        { label: 'Closed', value: 0, color: 'text-error-600' },
      ];

  return (
    <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
      {cards.map((card) => (
        <Card key={card.label} padding="md">
          <p className="text-body-sm text-neutral-500">{card.label}</p>
          {loading ? (
            <Skeleton className="h-8 w-16 mt-1" />
          ) : (
            <p className={`text-display-sm mt-1 ${card.color}`}>{card.value}</p>
          )}
        </Card>
      ))}
    </div>
  );
}

function JobRow({
  job,
}: {
  job: RecruiterJobSummary;
}) {
  const navigate = useNavigate();

  return (
    <tr className="border-b border-neutral-50 last:border-0 hover:bg-neutral-50/50 transition-colors">
      <td className="px-4 py-3">
        <button
          onClick={() => navigate(`/recruiter/jobs/${job.id}`)}
          className="text-body-md font-medium text-neutral-900 hover:text-secondary-700 text-left truncate max-w-[200px] sm:max-w-none"
        >
          {job.title}
        </button>
      </td>
      <td className="px-4 py-3">
        <Badge variant={statusVariant[job.status] ?? 'default'} size="sm">
          {statusLabels[job.status] ?? job.status}
        </Badge>
      </td>
      <td className="px-4 py-3 hidden sm:table-cell">
        <span className="text-body-sm text-neutral-600">{job.location || '—'}</span>
      </td>
      <td className="px-4 py-3 hidden md:table-cell">
        <span className="text-body-sm text-neutral-600">{employmentTypeLabels[job.employmentType] ?? job.employmentType}</span>
      </td>
      <td className="px-4 py-3 hidden lg:table-cell">
        <span className="text-body-sm text-neutral-500">{formatDate(job.createdAt)}</span>
      </td>
      <td className="px-4 py-3">
        <div className="flex items-center gap-1 justify-end">
          <Button variant="ghost" size="sm" onClick={() => navigate(`/recruiter/jobs/${job.id}`)}>
            View
          </Button>
        </div>
      </td>
    </tr>
  );
}

function JobCard({
  job,
}: {
  job: RecruiterJobSummary;
}) {
  const navigate = useNavigate();

  return (
    <Card padding="md">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0 flex-1">
          <button
            onClick={() => navigate(`/recruiter/jobs/${job.id}`)}
            className="text-body-md font-semibold text-neutral-900 hover:text-secondary-700 text-left truncate block"
          >
            {job.title}
          </button>
          <div className="flex items-center gap-2 mt-1 flex-wrap">
            {job.location && <span className="text-body-sm text-neutral-500">{job.location}</span>}
            <span className="text-body-sm text-neutral-500">{employmentTypeLabels[job.employmentType] ?? job.employmentType}</span>
          </div>
          <span className="text-body-sm text-neutral-400 mt-1 block">{formatDate(job.createdAt)}</span>
        </div>
      </div>
      <div className="flex items-center gap-2 mt-3 pt-3 border-t border-neutral-100">
        <Button variant="ghost" size="sm" onClick={() => navigate(`/recruiter/jobs/${job.id}`)}>
          View Details
        </Button>
      </div>
    </Card>
  );
}

export default function RecruiterJobsPage() {
  const navigate = useNavigate();
  const [jobs, setJobs] = useState<RecruiterJobSummary[]>([]);
  const [stats, setStats] = useState<RecruiterJobStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [statsLoading, setStatsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const fetchJobs = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const params: { page: number; size: number; status?: string } = { page: currentPage, size: 10 };
      if (statusFilter !== 'ALL') params.status = statusFilter;
      const data = await recruiterJobService.getJobs(params);
      setJobs(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch {
      setError('Failed to load jobs.');
    } finally {
      setLoading(false);
    }
  }, [currentPage, statusFilter]);

  const fetchStats = useCallback(async () => {
    try {
      setStatsLoading(true);
      const data = await recruiterJobService.getJobStats();
      setStats(data);
    } catch {
      // ignore
    } finally {
      setStatsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchJobs();
  }, [fetchJobs]);

  useEffect(() => {
    fetchStats();
  }, [fetchStats]);

  const handleFilterChange = (filter: StatusFilter) => {
    setStatusFilter(filter);
    setCurrentPage(0);
  };

  const filterTabs: { label: string; value: StatusFilter }[] = [
    { label: 'All', value: 'ALL' },
    { label: 'Draft', value: 'DRAFT' },
    { label: 'Published', value: 'PUBLISHED' },
    { label: 'Closed', value: 'CLOSED' },
  ];

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-heading-lg text-neutral-900">Jobs</h2>
          <p className="text-body-md text-neutral-500 mt-1">Manage your job postings</p>
        </div>
        <Button onClick={() => navigate('/recruiter/jobs/new')}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <line x1="12" y1="5" x2="12" y2="19" />
            <line x1="5" y1="12" x2="19" y2="12" />
          </svg>
          Create Job
        </Button>
      </div>

      <StatsCards stats={stats} loading={statsLoading} />

      <Card>
        <CardContent className="p-5">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
            <div className="flex items-center gap-1 bg-neutral-100 rounded-lg p-1">
              {filterTabs.map((tab) => (
                <button
                  key={tab.value}
                  onClick={() => handleFilterChange(tab.value)}
                  className={`px-3 py-1.5 text-body-sm font-medium rounded-md transition-colors ${
                    statusFilter === tab.value
                      ? 'bg-white text-neutral-900 shadow-sm'
                      : 'text-neutral-500 hover:text-neutral-700'
                  }`}
                >
                  {tab.label}
                </button>
              ))}
            </div>
            <span className="text-body-sm text-neutral-500">
              {totalElements} job{totalElements !== 1 ? 's' : ''}
            </span>
          </div>
        </CardContent>
      </Card>

      {loading ? (
        <div className="space-y-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="rounded-xl bg-white p-5 ring-1 ring-neutral-200/80 shadow-sm">
              <div className="flex items-center justify-between">
                <div className="flex-1">
                  <Skeleton className="h-5 w-48 mb-2" />
                  <Skeleton className="h-4 w-64" />
                </div>
                <Skeleton className="h-6 w-20 rounded-full" />
              </div>
            </div>
          ))}
        </div>
      ) : error ? (
        <Card>
          <CardContent className="p-8 text-center">
            <p className="text-body-md text-error-600 mb-4">{error}</p>
            <Button variant="outline" onClick={fetchJobs}>Try Again</Button>
          </CardContent>
        </Card>
      ) : jobs.length === 0 ? (
        <Card>
          <CardContent className="p-8">
            <EmptyState
              icon={
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <rect x="2" y="7" width="20" height="14" rx="2" ry="2" />
                  <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16" />
                </svg>
              }
              title="No jobs found"
              description={statusFilter !== 'ALL' ? 'No jobs match this filter. Try a different status.' : 'Create your first job posting to get started.'}
              action={
                statusFilter === 'ALL' ? (
                  <Button onClick={() => navigate('/recruiter/jobs/new')}>Create Job</Button>
                ) : undefined
              }
            />
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="hidden md:block">
            <Card padding="none">
              <div className="overflow-x-auto">
                <table className="w-full text-left">
                  <thead>
                    <tr className="border-b border-neutral-100">
                      <th className="px-4 py-3 text-overline">Title</th>
                      <th className="px-4 py-3 text-overline">Workplace</th>
                      <th className="px-4 py-3 text-overline hidden sm:table-cell">Location</th>
                      <th className="px-4 py-3 text-overline hidden md:table-cell">Type</th>
                      <th className="px-4 py-3 text-overline hidden lg:table-cell">Created</th>
                      <th className="px-4 py-3 text-overline text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {jobs.map((job) => (
                      <JobRow
                        key={job.id}
                        job={job}
                      />
                    ))}
                  </tbody>
                </table>
              </div>
            </Card>
          </div>
          <div className="md:hidden space-y-3">
            {jobs.map((job) => (
              <JobCard
                key={job.id}
                job={job}
              />
            ))}
          </div>
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 pt-4">
              <Button
                variant="outline"
                size="sm"
                disabled={currentPage === 0}
                onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
              >
                Previous
              </Button>
              <span className="text-body-sm text-neutral-600 px-3">
                Page {currentPage + 1} of {totalPages}
              </span>
              <Button
                variant="outline"
                size="sm"
                disabled={currentPage >= totalPages - 1}
                onClick={() => setCurrentPage((p) => Math.min(totalPages - 1, p + 1))}
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
