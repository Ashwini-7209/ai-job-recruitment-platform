import { useState, useEffect, useCallback, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Card, CardContent, Button, Input, Select, Badge, Skeleton, EmptyState, Pagination } from '@/components/ui';
import { recruiterCandidateService, type CandidateSearchResult, type CandidateSearchParams } from '@/services/recruiterCandidate.service';
import { useDebounce } from '@/hooks/useDebounce';

const sortOptions = [
  { value: 'name_asc', label: 'Name A-Z' },
  { value: 'name_desc', label: 'Name Z-A' },
  { value: 'experience_high', label: 'Most Experienced' },
  { value: 'experience_low', label: 'Least Experienced' },
  { value: 'newest', label: 'Recently Joined' },
];

const experienceOptions = [
  { value: '', label: 'Any Experience' },
  { value: '0-1', label: '0-1 years' },
  { value: '1-3', label: '1-3 years' },
  { value: '3-5', label: '3-5 years' },
  { value: '5-10', label: '5-10 years' },
  { value: '10+', label: '10+ years' },
];

interface FilterState {
  q: string;
  location: string;
  skills: string;
  experienceRange: string;
  jobTitle: string;
  sort: string;
}

function emptyFilters(): FilterState {
  return { q: '', location: '', skills: '', experienceRange: '', jobTitle: '', sort: 'name_asc' };
}

function parseSearchParams(sp: URLSearchParams): FilterState {
  return {
    q: sp.get('q') || '',
    location: sp.get('location') || '',
    skills: sp.get('skills') || '',
    experienceRange: sp.get('experienceRange') || '',
    jobTitle: sp.get('jobTitle') || '',
    sort: sp.get('sort') || 'name_asc',
  };
}

function CandidateCard({ candidate }: { candidate: CandidateSearchResult }) {
  const skills = candidate.skillsSummary?.split(',').slice(0, 4) || [];
  const skillCount = candidate.skillsSummary?.split(',').length || 0;

  return (
    <Card className="hover:border-secondary-200 hover:shadow-sm transition-all">
      <CardContent className="p-5">
        <div className="flex items-start gap-4">
          <div className="w-10 h-10 rounded-full bg-secondary-100 flex items-center justify-center shrink-0">
            {candidate.profileImageUrl ? (
              <img src={candidate.profileImageUrl} alt="" className="w-10 h-10 rounded-full object-cover" />
            ) : (
              <span className="text-body-sm font-medium text-secondary-700">
                {(candidate.fullName || '').split(' ').map(n => n[0]).join('').slice(0, 2)}
              </span>
            )}
          </div>
          <div className="min-w-0 flex-1">
            <h3 className="text-heading-sm text-neutral-900 truncate">{candidate.fullName}</h3>
            <p className="text-body-sm text-neutral-500 mt-0.5 truncate">
              {candidate.headline || candidate.currentJobTitle || candidate.email}
            </p>
            <div className="flex flex-wrap items-center gap-x-3 gap-y-1 mt-2 text-body-sm text-neutral-500">
              {candidate.location && (
                <span className="flex items-center gap-1">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z" />
                    <circle cx="12" cy="10" r="3" />
                  </svg>
                  {candidate.location}
                </span>
              )}
              {candidate.yearsOfExperience != null && (
                <span>{candidate.yearsOfExperience} yr experience</span>
              )}
              {candidate.hasResume && <Badge variant="success" size="sm">Resume</Badge>}
            </div>
            {skills.length > 0 && (
              <div className="flex flex-wrap gap-1.5 mt-3">
                {skills.map((skill, i) => (
                  <span key={i} className="inline-flex items-center rounded-full bg-neutral-100 px-2 py-0.5 text-xs font-medium text-neutral-600">
                    {skill.trim()}
                  </span>
                ))}
                {skillCount > 4 && (
                  <span className="text-xs text-neutral-400">+{skillCount - 4} more</span>
                )}
              </div>
            )}
            {candidate.educationSummary && (
              <p className="text-body-sm text-neutral-400 mt-2 truncate">{candidate.educationSummary}</p>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
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

export default function RecruiterCandidateSearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [filters, setFilters] = useState<FilterState>(() => parseSearchParams(searchParams));
  const [page, setPage] = useState(() => Number(searchParams.get('page')) || 0);
  const [candidates, setCandidates] = useState<CandidateSearchResult[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [mobileFilterOpen, setMobileFilterOpen] = useState(false);

  const debouncedQuery = useDebounce(filters.q, 350);
  const effectiveFilters = useMemo(() => ({ ...filters, q: debouncedQuery }), [filters, debouncedQuery]);

  const fetchCandidates = useCallback(async (filtersToUse: FilterState, pageNum: number) => {
    setIsLoading(true);
    setError(null);
    try {
      const params: CandidateSearchParams = { page: pageNum, size: 12, sort: filtersToUse.sort };
      if (debouncedQuery) params.q = debouncedQuery;
      if (filtersToUse.location) params.location = filtersToUse.location;
      if (filtersToUse.skills) params.skills = filtersToUse.skills;
      if (filtersToUse.jobTitle) params.jobTitle = filtersToUse.jobTitle;
      if (filtersToUse.experienceRange) {
        if (filtersToUse.experienceRange === '10+') {
          params.minExperience = 10;
        } else {
          const [min, max] = filtersToUse.experienceRange.split('-').map(Number);
          if (!isNaN(min)) params.minExperience = min;
          if (!isNaN(max)) params.maxExperience = max;
        }
      }
      const result = await recruiterCandidateService.searchCandidates(params);
      setCandidates(result?.content ?? []);
      setTotalElements(result?.totalElements ?? 0);
      setTotalPages(result?.totalPages ?? 0);
    } catch {
      setError('Failed to load candidates. Please try again.');
    } finally {
      setIsLoading(false);
    }
  }, [debouncedQuery]);

  useEffect(() => {
    fetchCandidates(effectiveFilters, page);
  }, [effectiveFilters, page, fetchCandidates]);

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

  const chips: { key: keyof FilterState; label: string; onRemove: () => void }[] = [];
  if (filters.q) chips.push({ key: 'q', label: `"${filters.q}"`, onRemove: () => removeFilter('q') });
  if (filters.location) chips.push({ key: 'location', label: filters.location, onRemove: () => removeFilter('location') });
  if (filters.skills) chips.push({ key: 'skills', label: `Skills: ${filters.skills}`, onRemove: () => removeFilter('skills') });
  if (filters.experienceRange) chips.push({ key: 'experienceRange', label: experienceOptions.find(o => o.value === filters.experienceRange)?.label || filters.experienceRange, onRemove: () => removeFilter('experienceRange') });
  if (filters.jobTitle) chips.push({ key: 'jobTitle', label: `Title: ${filters.jobTitle}`, onRemove: () => removeFilter('jobTitle') });

  const filterPanel = (
    <div className="space-y-4">
      <div>
        <label className="text-label-md text-neutral-700 block mb-1.5">Skills</label>
        <Input placeholder="e.g. Java, React" value={filters.skills} onChange={(e) => handleFilterChange(prev => ({ ...prev, skills: e.target.value }))} />
      </div>
      <div>
        <label className="text-label-md text-neutral-700 block mb-1.5">Location</label>
        <Input placeholder="City or state" value={filters.location} onChange={(e) => handleFilterChange(prev => ({ ...prev, location: e.target.value }))} />
      </div>
      <div>
        <label className="text-label-md text-neutral-700 block mb-1.5">Experience</label>
        <Select options={experienceOptions} value={filters.experienceRange} onChange={(e) => handleFilterChange(prev => ({ ...prev, experienceRange: e.target.value }))} />
      </div>
      <div>
        <label className="text-label-md text-neutral-700 block mb-1.5">Job Title</label>
        <Input placeholder="e.g. Software Engineer" value={filters.jobTitle} onChange={(e) => handleFilterChange(prev => ({ ...prev, jobTitle: e.target.value }))} />
      </div>
    </div>
  );

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-4 sm:space-y-6">
      <div>
        <h2 className="text-heading-lg text-neutral-900">Find Candidates</h2>
        <p className="text-body-md text-neutral-500 mt-1">
          {isLoading ? 'Searching...' : `${totalElements} candidate${totalElements !== 1 ? 's' : ''} found`}
        </p>
      </div>

      <Card>
        <CardContent className="p-4 sm:p-5">
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="flex-1">
              <Input
                placeholder="Search by name, skill, or headline..."
                value={filters.q}
                onChange={(e) => handleFilterChange(prev => ({ ...prev, q: e.target.value }))}
                leftIcon={
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" />
                  </svg>
                }
              />
            </div>
            <div className="w-full sm:w-44">
              <Select options={sortOptions} value={filters.sort} onChange={(e) => handleFilterChange(prev => ({ ...prev, sort: e.target.value }))} />
            </div>
            <Button variant="outline" className="sm:hidden flex items-center gap-2" onClick={() => setMobileFilterOpen(true)}>
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
          <button onClick={() => { setFilters(emptyFilters()); setPage(0); }} className="text-body-sm text-neutral-500 hover:text-neutral-700 transition-colors">
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
              {filterPanel}
              <div className="flex gap-2 pt-4">
                <Button variant="primary" className="flex-1" onClick={() => setMobileFilterOpen(false)}>Show Results</Button>
                <Button variant="outline" onClick={() => { setFilters(emptyFilters()); setMobileFilterOpen(false); }}>Clear All</Button>
              </div>
            </div>
          </div>
        </div>
      )}

      <div className="flex gap-6">
        <div className="hidden sm:block w-64 shrink-0">
          <Card className="sticky top-24">
            <CardContent className="p-5">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-heading-sm text-neutral-900">Filters</h3>
                <button onClick={() => { setFilters(emptyFilters()); setPage(0); }} className="text-body-sm text-secondary-600 hover:text-secondary-700 transition-colors">
                  Clear all
                </button>
              </div>
              {filterPanel}
            </CardContent>
          </Card>
        </div>

        <div className="flex-1 min-w-0">
          {isLoading && candidates.length === 0 ? (
            <div className="space-y-3">
              {Array.from({ length: 5 }).map((_, i) => (
                <Card key={i}><CardContent className="p-5">
                  <div className="flex items-start gap-4">
                    <Skeleton className="w-10 h-10 rounded-full" />
                    <div className="flex-1 space-y-2">
                      <Skeleton className="h-5 w-36" />
                      <Skeleton className="h-4 w-48" />
                      <div className="flex gap-2"><Skeleton className="h-5 w-16 rounded-full" /><Skeleton className="h-5 w-20 rounded-full" /></div>
                    </div>
                  </div>
                </CardContent></Card>
              ))}
            </div>
          ) : error ? (
            <Card><CardContent className="p-8 text-center">
              <p className="text-body-md text-error-600 mb-4">{error}</p>
              <Button variant="outline" onClick={() => fetchCandidates(effectiveFilters, page)}>Try Again</Button>
            </CardContent></Card>
          ) : candidates.length === 0 ? (
            <Card><CardContent className="p-0">
              <EmptyState
                icon={<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"><path d="M16 21v-2a4 4 0 00-4-4H6a4 4 0 00-4-4v2" /><circle cx="9" cy="7" r="4" /><path d="M22 21v-2a4 4 0 00-3-3.87" /><path d="M16 3.13a4 4 0 010 7.75" /></svg>}
                title="No candidates found"
                description="Try adjusting your search or filters."
              />
            </CardContent></Card>
          ) : (
            <>
              <div className="space-y-3">
                {candidates.map((c) => <CandidateCard key={c.candidateId} candidate={c} />)}
              </div>
              <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
            </>
          )}
        </div>
      </div>
    </div>
  );
}
