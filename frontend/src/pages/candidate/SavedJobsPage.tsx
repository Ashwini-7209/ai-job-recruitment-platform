import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, Button, Badge, Input, Select, EmptyState, Skeleton } from '@/components/ui';
import { savedJobService, type SavedJob } from '@/services/savedJob.service';
import { useSaveJob } from '@/hooks/useSaveJob';

const workplaceOptions = [
  { value: '', label: 'All Workplaces' },
  { value: 'REMOTE', label: 'Remote' },
  { value: 'ONSITE', label: 'Onsite' },
  { value: 'HYBRID', label: 'Hybrid' },
];

const employmentOptions = [
  { value: '', label: 'All Types' },
  { value: 'FULL_TIME', label: 'Full Time' },
  { value: 'PART_TIME', label: 'Part Time' },
  { value: 'CONTRACT', label: 'Contract' },
  { value: 'INTERNSHIP', label: 'Internship' },
  { value: 'FREELANCE', label: 'Freelance' },
];

const jobStatusOptions = [
  { value: '', label: 'All Statuses' },
  { value: 'PUBLISHED', label: 'Open' },
  { value: 'CLOSED', label: 'Closed' },
];

const jobStatusLabels: Record<string, string> = {
  PUBLISHED: 'Open',
  CLOSED: 'Closed',
  DRAFT: 'Draft',
};

const jobStatusVariant: Record<string, 'success' | 'error' | 'default'> = {
  PUBLISHED: 'success',
  CLOSED: 'error',
  DRAFT: 'default',
};

function formatSalary(min: number | null, max: number | null): string | null {
  if (!min && !max) return null;
  const fmt = (n: number) => n >= 1000 ? `$${(n / 1000).toFixed(0)}k` : `$${n}`;
  if (min && max) return `${fmt(min)} - ${fmt(max)}`;
  if (min) return `From ${fmt(min)}`;
  return `Up to ${fmt(max!)}`;
}

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function SavedJobCard({ job, onRemove }: { job: SavedJob; onRemove: (jobId: number) => void }) {
  const navigate = useNavigate();
  const { isSaved, isLoading, toggle } = useSaveJob(true);

  const handleToggle = async (e: React.MouseEvent) => {
    e.stopPropagation();
    const wasSaved = isSaved;
    await toggle(job.jobId);
    if (wasSaved) {
      onRemove(job.jobId);
    }
  };

  return (
    <div className="rounded-xl border border-neutral-200 bg-white p-4 sm:p-5 hover:border-secondary-200 hover:shadow-sm transition-all duration-150">
      <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-3">
        <div className="min-w-0 flex-1">
          <div className="flex items-start justify-between gap-2">
            <div className="min-w-0">
              <h3
                className="text-heading-sm text-neutral-900 truncate cursor-pointer hover:text-secondary-600 transition-colors"
onClick={() => navigate('/candidate/jobs/' + job.jobId)}
              >
                {job.jobTitle}
              </h3>
              <div className="flex flex-wrap items-center gap-x-3 gap-y-1 mt-1.5 text-body-sm text-neutral-500">
                {job.location && (
                  <span className="flex items-center gap-1">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z" />
                      <circle cx="12" cy="10" r="3" />
                    </svg>
                    {job.location}
                  </span>
                )}
                <span>{job.employmentType.replace('_', ' ')}</span>
                <span>{job.workplaceType.replace('_', ' ')}</span>
                {formatSalary(job.salaryMin, job.salaryMax) && (
                  <span className="font-medium text-neutral-700">{formatSalary(job.salaryMin, job.salaryMax)}</span>
                )}
              </div>
            </div>
            <div className="flex items-center gap-2 shrink-0">
              <Badge variant={jobStatusVariant[job.jobStatus]} size="sm">
                {jobStatusLabels[job.jobStatus]}
              </Badge>
            </div>
          </div>
          {job.skills && (
            <div className="flex flex-wrap gap-1.5 mt-3">
              {job.skills.split(',').slice(0, 5).map((skill, i) => (
                <span key={i} className="inline-flex items-center rounded-full bg-neutral-100 px-2 py-0.5 text-xs font-medium text-neutral-600">
                  {skill.trim()}
                </span>
              ))}
            </div>
          )}
          <div className="flex items-center gap-4 mt-3 text-caption text-neutral-400">
            <span>Saved {formatDate(job.savedAt)}</span>
            {job.applied && (
              <Badge variant="info" size="sm">Applied</Badge>
            )}
            {job.deadline && (
              <span>Deadline: {formatDate(job.deadline)}</span>
            )}
          </div>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          <Button
            variant="ghost"
            size="sm"
            onClick={handleToggle}
            disabled={isLoading}
            className={isSaved ? 'text-error-500 hover:text-error-600 hover:bg-error-50' : 'text-neutral-400 hover:text-error-500'}
          >
            {isLoading ? (
              <div className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
            ) : (
              <svg width="18" height="18" viewBox="0 0 24 24" fill={isSaved ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
              </svg>
            )}
          </Button>
          <Button variant="outline" size="sm" onClick={() => navigate('/candidate/jobs/' + job.jobId)}>View Job</Button>
        </div>
      </div>
    </div>
  );
}

export default function SavedJobsPage() {
  const navigate = useNavigate();
  const [savedJobs, setSavedJobs] = useState<SavedJob[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [workplaceFilter, setWorkplaceFilter] = useState('');
  const [employmentFilter, setEmploymentFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [totalElements, setTotalElements] = useState(0);

  const fetchSavedJobs = useCallback(async (pageNum: number, append = false) => {
    setIsLoading(true);
    try {
      const result = await savedJobService.getSavedJobs({
        q: searchQuery || undefined,
        location: undefined,
        workplaceType: workplaceFilter || undefined,
        employmentType: employmentFilter || undefined,
        jobStatus: statusFilter || undefined,
        page: pageNum,
        size: 20,
      });
      if (append) {
        setSavedJobs((prev) => [...prev, ...result.content]);
      } else {
        setSavedJobs(result.content);
      }
      setHasMore(!result.last);
      setTotalElements(result.totalElements);
    } catch {
      // Error handled silently
    } finally {
      setIsLoading(false);
    }
  }, [searchQuery, workplaceFilter, employmentFilter, statusFilter]);

  useEffect(() => {
    setPage(0);
    fetchSavedJobs(0);
  }, [fetchSavedJobs]);

  const handleSearch = (value: string) => {
    setSearchQuery(value);
    setPage(0);
  };

  const handleLoadMore = () => {
    const nextPage = page + 1;
    setPage(nextPage);
    fetchSavedJobs(nextPage, true);
  };

  const handleRemove = (jobId: number) => {
    setSavedJobs((prev) => prev.filter((sj) => sj.jobId !== jobId));
    setTotalElements((prev) => prev - 1);
  };

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
      <div>
        <h1 className="text-heading-lg text-neutral-900">Saved Jobs</h1>
        <p className="text-body-md text-neutral-500 mt-1">
          {totalElements} job{totalElements !== 1 ? 's' : ''} saved
        </p>
      </div>

      <Card>
        <CardContent className="p-5">
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="flex-1">
              <Input
                placeholder="Search saved jobs..."
                value={searchQuery}
                onChange={(e) => handleSearch(e.target.value)}
                leftIcon={
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <circle cx="11" cy="11" r="8" />
                    <line x1="21" y1="21" x2="16.65" y2="16.65" />
                  </svg>
                }
              />
            </div>
            <div className="w-full sm:w-40">
              <Select
                options={workplaceOptions}
                value={workplaceFilter}
                onChange={(e) => { setWorkplaceFilter(e.target.value); setPage(0); }}
                placeholder="Workplace"
              />
            </div>
            <div className="w-full sm:w-40">
              <Select
                options={employmentOptions}
                value={employmentFilter}
                onChange={(e) => { setEmploymentFilter(e.target.value); setPage(0); }}
                placeholder="Type"
              />
            </div>
            <div className="w-full sm:w-36">
              <Select
                options={jobStatusOptions}
                value={statusFilter}
                onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
                placeholder="Status"
              />
            </div>
          </div>
        </CardContent>
      </Card>

      {isLoading && savedJobs.length === 0 ? (
        <div className="space-y-4">
          {Array.from({ length: 3 }).map((_, i) => (
            <Card key={i}>
              <CardContent className="p-5">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1 space-y-3">
                    <Skeleton className="h-5 w-48" />
                    <Skeleton className="h-4 w-32" />
                    <div className="flex gap-2">
                      <Skeleton className="h-5 w-16 rounded-full" />
                      <Skeleton className="h-5 w-20 rounded-full" />
                    </div>
                  </div>
                  <Skeleton className="h-8 w-8 rounded-lg" />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : savedJobs.length === 0 ? (
        <Card>
          <CardContent className="p-0">
            <EmptyState
              icon={
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
                </svg>
              }
              title={searchQuery ? 'No matching saved jobs' : 'No saved jobs yet'}
              description={
                searchQuery
                  ? 'Try adjusting your search terms.'
                  : 'Browse jobs and save the ones you like to keep track of them here.'
              }
              action={
                !searchQuery ? (
                  <Button onClick={() => navigate('/candidate/jobs')}>
                    Browse Jobs
                  </Button>
                ) : undefined
              }
            />
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="space-y-3">
            {savedJobs.map((job) => (
              <SavedJobCard key={job.savedJobId} job={job} onRemove={handleRemove} />
            ))}
          </div>
          {hasMore && (
            <div className="text-center">
              <Button variant="outline" onClick={handleLoadMore} disabled={isLoading}>
                {isLoading ? 'Loading...' : 'Load more'}
              </Button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
