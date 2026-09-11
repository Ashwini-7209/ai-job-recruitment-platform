import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, Button, Input, Select, Badge, Skeleton, EmptyState } from '@/components/ui';
import { jobService, type JobListItem, type JobSearchParams } from '@/services/job.service';
import { savedJobService } from '@/services/savedJob.service';

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

const sortOptions = [
  { value: 'newest', label: 'Newest First' },
  { value: 'oldest', label: 'Oldest First' },
  { value: 'salary_high', label: 'Salary: High to Low' },
  { value: 'salary_low', label: 'Salary: Low to High' },
];

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

function JobCard({ job, onToggleSave }: { job: JobListItem; onToggleSave: (jobId: number, saved: boolean) => void }) {
  const navigate = useNavigate();
  const [saving, setSaving] = useState(false);

  const handleSaveToggle = async (e: React.MouseEvent) => {
    e.stopPropagation();
    setSaving(true);
    try {
      if (job.saved) {
        await savedJobService.unsaveJob(job.jobId);
        onToggleSave(job.jobId, false);
      } else {
        await savedJobService.saveJob(job.jobId);
        onToggleSave(job.jobId, true);
      }
    } catch {
      // silently fail
    } finally {
      setSaving(false);
    }
  };

  return (
    <div
      className="rounded-xl border border-neutral-200 bg-white p-4 sm:p-5 hover:border-primary-200 hover:shadow-sm transition-all duration-150 cursor-pointer"
      onClick={() => navigate(`/candidate/applications`)}
    >
      <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-3">
        <div className="min-w-0 flex-1">
          <div className="flex items-start justify-between gap-2">
            <div className="min-w-0">
              <h3 className="text-heading-sm text-neutral-900 truncate hover:text-primary-600 transition-colors">
                {job.title}
              </h3>
              <p className="text-body-sm text-neutral-500 mt-0.5">{job.companyName}</p>
            </div>
          </div>
          <div className="flex flex-wrap items-center gap-x-3 gap-y-1 mt-2 text-body-sm text-neutral-500">
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
          {job.skills && (
            <div className="flex flex-wrap gap-1.5 mt-3">
              {job.skills.split(',').slice(0, 5).map((skill, i) => (
                <span key={i} className="inline-flex items-center rounded-full bg-neutral-100 px-2 py-0.5 text-xs font-medium text-neutral-600">
                  {skill.trim()}
                </span>
              ))}
              {job.skills.split(',').length > 5 && (
                <span className="text-xs text-neutral-400">+{job.skills.split(',').length - 5} more</span>
              )}
            </div>
          )}
          <div className="flex items-center gap-4 mt-3 text-caption text-neutral-400">
            <span>Posted {formatDate(job.createdAt)}</span>
            {job.applied && <Badge variant="info" size="sm">Applied</Badge>}
            {job.deadline && <span>Deadline: {formatDate(job.deadline)}</span>}
          </div>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          <Button
            variant="ghost"
            size="sm"
            onClick={handleSaveToggle}
            disabled={saving}
            className={job.saved ? 'text-error-500 hover:text-error-600 hover:bg-error-50' : 'text-neutral-400 hover:text-error-500'}
          >
            {saving ? (
              <div className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
            ) : (
              <svg width="18" height="18" viewBox="0 0 24 24" fill={job.saved ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
              </svg>
            )}
          </Button>
          <Button variant="outline" size="sm">View Details</Button>
        </div>
      </div>
    </div>
  );
}

export default function CandidateJobSearchPage() {
  const [jobs, setJobs] = useState<JobListItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [workplaceFilter, setWorkplaceFilter] = useState('');
  const [employmentFilter, setEmploymentFilter] = useState('');
  const [sortOption, setSortOption] = useState('newest');
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [totalElements, setTotalElements] = useState(0);

  const fetchJobs = useCallback(async (pageNum: number, append = false) => {
    setIsLoading(true);
    try {
      const params: JobSearchParams = {
        q: searchQuery || undefined,
        workplaceType: workplaceFilter || undefined,
        employmentType: employmentFilter || undefined,
        page: pageNum,
        size: 20,
        sort: sortOption,
      };
      const result = await jobService.searchJobs(params);
      if (append) {
        setJobs((prev) => [...prev, ...result.content]);
      } else {
        setJobs(result.content);
      }
      setHasMore(!result.last);
      setTotalElements(result.totalElements);
    } catch {
      // silently fail
    } finally {
      setIsLoading(false);
    }
  }, [searchQuery, workplaceFilter, employmentFilter, sortOption]);

  useEffect(() => {
    setPage(0);
    fetchJobs(0);
  }, [fetchJobs]);

  const handleSearch = (value: string) => {
    setSearchQuery(value);
    setPage(0);
  };

  const handleLoadMore = () => {
    const nextPage = page + 1;
    setPage(nextPage);
    fetchJobs(nextPage, true);
  };

  const handleToggleSave = (jobId: number, saved: boolean) => {
    setJobs((prev) => prev.map((j) => j.jobId === jobId ? { ...j, saved } : j));
  };

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
      <div>
        <h1 className="text-heading-lg text-neutral-900">Find Jobs</h1>
        <p className="text-body-md text-neutral-500 mt-1">
          {totalElements} job{totalElements !== 1 ? 's' : ''} available
        </p>
      </div>

      <Card>
        <CardContent className="p-5">
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="flex-1">
              <Input
                placeholder="Search jobs by title, skill, or company..."
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
            <div className="w-full sm:w-40">
              <Select
                options={sortOptions}
                value={sortOption}
                onChange={(e) => { setSortOption(e.target.value); setPage(0); }}
                placeholder="Sort by"
              />
            </div>
          </div>
        </CardContent>
      </Card>

      {isLoading && jobs.length === 0 ? (
        <div className="space-y-4">
          {Array.from({ length: 5 }).map((_, i) => (
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
      ) : jobs.length === 0 ? (
        <Card>
          <CardContent className="p-0">
            <EmptyState
              icon={
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="11" cy="11" r="8" />
                  <line x1="21" y1="21" x2="16.65" y2="16.65" />
                </svg>
              }
              title={searchQuery ? 'No matching jobs found' : 'No jobs available'}
              description={
                searchQuery
                  ? 'Try adjusting your search terms or filters.'
                  : 'Check back later for new job postings.'
              }
            />
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="space-y-3">
            {jobs.map((job) => (
              <JobCard key={job.jobId} job={job} onToggleSave={handleToggleSave} />
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
