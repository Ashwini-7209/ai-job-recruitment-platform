import { useState, useEffect, useCallback, useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Card, CardContent, Button, Input, Select, Badge, Skeleton, EmptyState, Pagination } from '@/components/ui';
import { jobService, type JobListItem, type JobSearchParams } from '@/services/job.service';
import { savedJobService } from '@/services/savedJob.service';
import { useDebounce } from '@/hooks/useDebounce';

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

const experienceOptions = [
  { value: '', label: 'Any Experience' },
  { value: '0-1', label: '0-1 years' },
  { value: '1-3', label: '1-3 years' },
  { value: '3-5', label: '3-5 years' },
  { value: '5-10', label: '5-10 years' },
  { value: '10+', label: '10+ years' },
];

const datePostedOptions = [
  { value: '', label: 'Any Time' },
  { value: '1', label: 'Past 24 hours' },
  { value: '7', label: 'Past Week' },
  { value: '30', label: 'Past Month' },
  { value: '90', label: 'Past 3 Months' },
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
    } catch { /* silently fail */ } finally {
      setSaving(false);
    }
  };

  return (
    <div
      className="rounded-xl border border-neutral-200 bg-white p-4 sm:p-5 hover:border-secondary-200 hover:shadow-sm transition-all duration-150 cursor-pointer"
      onClick={() => navigate('/candidate/jobs/' + job.jobId)}
    >
      <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-3">
        <div className="min-w-0 flex-1">
          <div className="flex items-start justify-between gap-2">
            <div className="min-w-0">
              <h3 className="text-heading-sm text-neutral-900 truncate hover:text-secondary-600 transition-colors">
                {job.title}
              </h3>
              <p className="text-body-sm text-neutral-500 mt-0.5">{job.recruiterName || job.companyName}</p>
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
            {job.experienceMin != null && job.experienceMax != null && (
              <span>{job.experienceMin}-{job.experienceMax} yr</span>
            )}
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
            <span>Posted {formatDate(job.publishedAt || job.createdAt)}</span>
            {job.applied && <Badge variant="info" size="sm">Applied</Badge>}
            {job.applicationDeadline && <span>Deadline: {formatDate(job.applicationDeadline)}</span>}
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

function FilterChip({ label, onRemove }: { label: string; onRemove: () => void }) {
  return (
    <span className="inline-flex items-center gap-1 rounded-full bg-secondary-50 border border-secondary-200 px-2.5 py-1 text-xs font-medium text-secondary-700">
      {label}
      <button onClick={onRemove} className="hover:text-secondary-900 transition-colors">
        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
          <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
        </svg>
      </button>
    </span>
  );
}

function FilterPanel({
  filters, setFilters, onClose, isMobile
}: {
  filters: FilterState;
  setFilters: (f: FilterState | ((prev: FilterState) => FilterState)) => void;
  onClose?: () => void;
  isMobile?: boolean;
}) {
  const updateFilter = <K extends keyof FilterState>(key: K, value: FilterState[K]) => {
    setFilters(prev => ({ ...prev, [key]: value }));
  };

  const panel = (
    <div className="space-y-4">
      <div>
        <label className="text-label-md text-neutral-700 block mb-1.5">Skills</label>
        <Input
          placeholder="e.g. Java, React"
          value={filters.skills}
          onChange={(e) => updateFilter('skills', e.target.value)}
        />
      </div>
      <div>
        <label className="text-label-md text-neutral-700 block mb-1.5">Company</label>
        <Input
          placeholder="Company name"
          value={filters.companyName}
          onChange={(e) => updateFilter('companyName', e.target.value)}
        />
      </div>
      <div>
        <label className="text-label-md text-neutral-700 block mb-1.5">Location</label>
        <Input
          placeholder="City, state, or remote"
          value={filters.location}
          onChange={(e) => updateFilter('location', e.target.value)}
        />
      </div>
      <div>
        <label className="text-label-md text-neutral-700 block mb-1.5">Workplace</label>
        <Select
          options={workplaceOptions}
          value={filters.workplaceType}
          onChange={(e) => updateFilter('workplaceType', e.target.value)}
        />
      </div>
      <div>
        <label className="text-label-md text-neutral-700 block mb-1.5">Employment Type</label>
        <Select
          options={employmentOptions}
          value={filters.employmentType}
          onChange={(e) => updateFilter('employmentType', e.target.value)}
        />
      </div>
      <div>
        <label className="text-label-md text-neutral-700 block mb-1.5">Experience</label>
        <Select
          options={experienceOptions}
          value={filters.experienceRange}
          onChange={(e) => updateFilter('experienceRange', e.target.value)}
        />
      </div>
      <div>
        <label className="text-label-md text-neutral-700 block mb-1.5">Salary Range</label>
        <div className="grid grid-cols-2 gap-2">
          <Input
            type="number"
            placeholder="Min"
            value={filters.salaryMin}
            onChange={(e) => updateFilter('salaryMin', e.target.value)}
          />
          <Input
            type="number"
            placeholder="Max"
            value={filters.salaryMax}
            onChange={(e) => updateFilter('salaryMax', e.target.value)}
          />
        </div>
      </div>
      <div>
        <label className="text-label-md text-neutral-700 block mb-1.5">Date Posted</label>
        <Select
          options={datePostedOptions}
          value={filters.datePosted}
          onChange={(e) => updateFilter('datePosted', e.target.value)}
        />
      </div>
      {isMobile && (
        <div className="flex gap-2 pt-2">
          <Button variant="primary" className="flex-1" onClick={onClose}>Show Results</Button>
          <Button variant="outline" onClick={() => {
            setFilters(emptyFilters());
            onClose?.();
          }}>Clear All</Button>
        </div>
      )}
    </div>
  );

  if (isMobile) return panel;

  return (
    <Card className="sticky top-24">
      <CardContent className="p-5">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-heading-sm text-neutral-900">Filters</h3>
          <button
            onClick={() => setFilters(emptyFilters())}
            className="text-body-sm text-secondary-600 hover:text-secondary-700 transition-colors"
          >
            Clear all
          </button>
        </div>
        {panel}
      </CardContent>
    </Card>
  );
}

interface FilterState {
  q: string;
  location: string;
  employmentType: string;
  workplaceType: string;
  experienceRange: string;
  salaryMin: string;
  salaryMax: string;
  skills: string;
  companyName: string;
  datePosted: string;
  sort: string;
}

function emptyFilters(): FilterState {
  return { q: '', location: '', employmentType: '', workplaceType: '', experienceRange: '', salaryMin: '', salaryMax: '', skills: '', companyName: '', datePosted: '', sort: 'newest' };
}

function parseSearchParams(sp: URLSearchParams): FilterState {
  return {
    q: sp.get('q') || '',
    location: sp.get('location') || '',
    employmentType: sp.get('employmentType') || '',
    workplaceType: sp.get('workplaceType') || '',
    experienceRange: sp.get('experienceRange') || '',
    salaryMin: sp.get('salaryMin') || '',
    salaryMax: sp.get('salaryMax') || '',
    skills: sp.get('skills') || '',
    companyName: sp.get('companyName') || '',
    datePosted: sp.get('datePosted') || '',
    sort: sp.get('sort') || 'newest',
  };
}

function buildSearchParams(filters: FilterState, page: number): JobSearchParams {
  const params: JobSearchParams = { page, size: 12, sort: filters.sort };
  if (filters.q) params.q = filters.q;
  if (filters.location) params.location = filters.location;
  if (filters.employmentType) params.employmentType = filters.employmentType;
  if (filters.workplaceType) params.workplaceType = filters.workplaceType;
  if (filters.skills) params.skills = filters.skills;
  if (filters.companyName) params.companyName = filters.companyName;
  if (filters.salaryMin) params.salaryMin = Number(filters.salaryMin);
  if (filters.salaryMax) params.salaryMax = Number(filters.salaryMax);
  if (filters.experienceRange) {
    if (filters.experienceRange === '10+') {
      params.experienceMin = 10;
    } else {
      const [min, max] = filters.experienceRange.split('-').map(Number);
      if (!isNaN(min)) params.experienceMin = min;
      if (!isNaN(max)) params.experienceMax = max;
    }
  }
  if (filters.datePosted) {
    const d = new Date();
    d.setDate(d.getDate() - Number(filters.datePosted));
    params.postedAfter = d.toISOString();
  }
  return params;
}

function getActiveChips(filters: FilterState, removeFilter: (key: keyof FilterState) => void) {
  const chips: { key: keyof FilterState; label: string }[] = [];
  if (filters.q) chips.push({ key: 'q', label: `"${filters.q}"` });
  if (filters.location) chips.push({ key: 'location', label: filters.location });
  if (filters.employmentType) chips.push({ key: 'employmentType', label: employmentOptions.find(o => o.value === filters.employmentType)?.label || filters.employmentType });
  if (filters.workplaceType) chips.push({ key: 'workplaceType', label: workplaceOptions.find(o => o.value === filters.workplaceType)?.label || filters.workplaceType });
  if (filters.experienceRange) chips.push({ key: 'experienceRange', label: experienceOptions.find(o => o.value === filters.experienceRange)?.label || filters.experienceRange });
  if (filters.salaryMin) chips.push({ key: 'salaryMin', label: `Min $${filters.salaryMin}` });
  if (filters.salaryMax) chips.push({ key: 'salaryMax', label: `Max $${filters.salaryMax}` });
  if (filters.skills) chips.push({ key: 'skills', label: `Skills: ${filters.skills}` });
  if (filters.companyName) chips.push({ key: 'companyName', label: `Company: ${filters.companyName}` });
  if (filters.datePosted) chips.push({ key: 'datePosted', label: datePostedOptions.find(o => o.value === filters.datePosted)?.label || filters.datePosted });
  return chips.map(c => ({ ...c, onRemove: () => removeFilter(c.key) }));
}

export default function CandidateJobSearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [filters, setFilters] = useState<FilterState>(() => parseSearchParams(searchParams));
  const [page, setPage] = useState(() => Number(searchParams.get('page')) || 0);
  const [jobs, setJobs] = useState<JobListItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [mobileFilterOpen, setMobileFilterOpen] = useState(false);

  const debouncedQuery = useDebounce(filters.q, 350);

  const effectiveFilters = useMemo(() => ({ ...filters, q: debouncedQuery }), [filters, debouncedQuery]);

  const fetchJobs = useCallback(async (filtersToUse: FilterState, pageNum: number) => {
    setIsLoading(true);
    setError(null);
    try {
      const params = buildSearchParams({ ...filtersToUse, q: debouncedQuery }, pageNum);
      const result = await jobService.searchJobs(params);
      setJobs(result.content);
      setTotalElements(result.totalElements);
      setTotalPages(result.totalPages);
    } catch {
      setError('Failed to load jobs. Please try again.');
    } finally {
      setIsLoading(false);
    }
  }, [debouncedQuery]);

  useEffect(() => {
    fetchJobs(effectiveFilters, page);
  }, [effectiveFilters, page, fetchJobs]);

  useEffect(() => {
    const next = new URLSearchParams();
    Object.entries(filters).forEach(([k, v]) => { if (v) next.set(k, v); });
    if (page > 0) next.set('page', String(page));
    setSearchParams(next, { replace: true });
  }, [filters, page, setSearchParams]);

  const handleFilterChange = (newFilters: FilterState | ((prev: FilterState) => FilterState)) => {
    setFilters(newFilters);
    setPage(0);
  };

  const removeFilter = (key: keyof FilterState) => {
    setFilters(prev => ({ ...prev, [key]: '' }));
    setPage(0);
  };

  const chips = getActiveChips(filters, removeFilter);

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-4 sm:space-y-6">
      <div>
        <h1 className="text-heading-lg text-neutral-900">Find Jobs</h1>
        <p className="text-body-md text-neutral-500 mt-1">
          {isLoading ? 'Searching...' : `${totalElements} job${totalElements !== 1 ? 's' : ''} available`}
        </p>
      </div>

      <Card>
        <CardContent className="p-4 sm:p-5">
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="flex-1">
              <Input
                placeholder="Search by title, skill, or company..."
                value={filters.q}
                onChange={(e) => handleFilterChange(prev => ({ ...prev, q: e.target.value }))}
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
                options={sortOptions}
                value={filters.sort}
                onChange={(e) => handleFilterChange(prev => ({ ...prev, sort: e.target.value }))}
              />
            </div>
            <Button
              variant="outline"
              className="sm:hidden flex items-center gap-2"
              onClick={() => setMobileFilterOpen(true)}
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3" />
              </svg>
              Filters
              {chips.length > 0 && (
                <span className="h-5 w-5 rounded-full bg-secondary-600 text-white text-xs flex items-center justify-center">{chips.length}</span>
              )}
            </Button>
          </div>
        </CardContent>
      </Card>

      {chips.length > 0 && (
        <div className="flex flex-wrap gap-2">
          {chips.map((chip) => (
            <FilterChip key={chip.key} label={chip.label} onRemove={chip.onRemove} />
          ))}
          <button
            onClick={() => { setFilters(emptyFilters()); setPage(0); }}
            className="text-body-sm text-neutral-500 hover:text-neutral-700 transition-colors"
          >
            Clear all
          </button>
        </div>
      )}

      {mobileFilterOpen && (
        <div className="fixed inset-0 z-50 sm:hidden">
          <div className="absolute inset-0 bg-black/40" onClick={() => setMobileFilterOpen(false)} />
          <div className="absolute right-0 top-0 h-full w-80 max-w-full bg-white shadow-xl overflow-y-auto">
            <div className="flex items-center justify-between p-4 border-b border-neutral-100">
              <h3 className="text-heading-sm text-neutral-900">Filters</h3>
              <button onClick={() => setMobileFilterOpen(false)} className="text-neutral-400 hover:text-neutral-600">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
                </svg>
              </button>
            </div>
            <div className="p-4">
              <FilterPanel
                filters={filters}
                setFilters={handleFilterChange}
                onClose={() => setMobileFilterOpen(false)}
                isMobile
              />
            </div>
          </div>
        </div>
      )}

      <div className="flex gap-6">
        <div className="hidden sm:block w-64 shrink-0">
          <FilterPanel filters={filters} setFilters={handleFilterChange} />
        </div>

        <div className="flex-1 min-w-0">
          {isLoading && jobs.length === 0 ? (
            <div className="space-y-3">
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
          ) : error ? (
            <Card>
              <CardContent className="p-8 text-center">
                <p className="text-body-md text-error-600 mb-4">{error}</p>
                <Button variant="outline" onClick={() => fetchJobs(effectiveFilters, page)}>Try Again</Button>
              </CardContent>
            </Card>
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
                  title="No matching jobs found"
                  description="Try adjusting your search terms or filters."
                />
              </CardContent>
            </Card>
          ) : (
            <>
              <div className="space-y-3">
                {jobs.map((job) => (
                  <JobCard
                    key={job.jobId}
                    job={job}
                    onToggleSave={(jobId, saved) => setJobs(prev => prev.map(j => j.jobId === jobId ? { ...j, saved } : j))}
                  />
                ))}
              </div>
              <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
            </>
          )}
        </div>
      </div>
    </div>
  );
}
