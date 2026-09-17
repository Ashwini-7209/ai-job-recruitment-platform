import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, Badge, Button, Input, Select, Skeleton, EmptyState } from '@/components/ui';
import { applicationService, type ApplicationListParams } from '@/services/application.service';
import type { CandidateApplicationSummary } from '@/types/candidate';

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
  { value: 'newest', label: 'Newest First' },
  { value: 'oldest', label: 'Oldest First' },
  { value: 'updated', label: 'Recently Updated' },
];

const statusBadgeVariant: Record<string, 'info' | 'warning' | 'success' | 'error' | 'default' | 'primary'> = {
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

function formatDate(dateStr: string): string {
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return '-';
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function formatEmploymentType(type: string): string {
  if (!type) return '';
  return type.replace('_', ' ').toLowerCase().replace(/\b\w/g, (l) => l.toUpperCase());
}

function ApplicationCard({ app, onClick }: { app: CandidateApplicationSummary; onClick: () => void }) {
  return (
    <div
      className="rounded-lg border border-neutral-200 p-4 hover:border-primary-200 hover:shadow-sm transition-all duration-150 cursor-pointer"
      onClick={onClick}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0 flex-1">
          <h3 className="text-body-md font-semibold text-neutral-900 truncate">{app.jobTitle}</h3>
          <div className="flex flex-wrap items-center gap-x-3 gap-y-1 mt-1.5 text-body-sm text-neutral-500">
            <span className="flex items-center gap-1">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z" />
                <circle cx="12" cy="10" r="3" />
              </svg>
              {app.location || 'Remote'}
            </span>
            <span>{formatEmploymentType(app.employmentType)}</span>
            <span>{app.workplaceType}</span>
          </div>
        </div>
        <Badge variant={statusBadgeVariant[app.status]} size="sm">
          {statusLabels[app.status]}
        </Badge>
      </div>
      <div className="flex items-center justify-between mt-3 pt-3 border-t border-neutral-100">
        <div className="flex items-center gap-3 text-caption">
          <span>Applied {formatDate(app.appliedAt)}</span>
          {app.hasResume && (
            <span className="flex items-center gap-1 text-success-600">
              <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <polyline points="20,6 9,17 4,12" />
              </svg>
              Resume attached
            </span>
          )}
        </div>
        <Button variant="ghost" size="sm">View Details</Button>
      </div>
    </div>
  );
}

function ApplicationCardSkeleton() {
  return (
    <div className="rounded-lg border border-neutral-200 p-4">
      <div className="flex items-start justify-between gap-3">
        <div className="flex-1">
          <Skeleton className="h-5 w-48 mb-2" />
          <Skeleton className="h-4 w-64" />
        </div>
        <Skeleton className="h-6 w-20 rounded-full" />
      </div>
      <div className="flex items-center justify-between mt-3 pt-3 border-t border-neutral-100">
        <Skeleton className="h-3 w-32" />
        <Skeleton className="h-8 w-24" />
      </div>
    </div>
  );
}

export default function CandidateApplicationsPage() {
  const navigate = useNavigate();
  const [applications, setApplications] = useState<CandidateApplicationSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [sortOrder, setSortOrder] = useState('newest');

  const fetchApplications = useCallback(async (params: ApplicationListParams) => {
    try {
      setLoading(true);
      setError(null);
      const data = await applicationService.listApplications(params);
      setApplications(data.content);
      setTotalElements(data.totalElements);
      setTotalPages(data.totalPages);
    } catch (err) {
      setError('Failed to load applications. Please try again.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchApplications({
      q: searchQuery || undefined,
      status: statusFilter || undefined,
      page: currentPage,
      size: 10,
      sort: sortOrder,
    });
  }, [searchQuery, statusFilter, sortOrder, currentPage, fetchApplications]);

  const handleSearch = (value: string) => {
    setSearchQuery(value);
    setCurrentPage(0);
  };

  const handleStatusChange = (value: string) => {
    setStatusFilter(value);
    setCurrentPage(0);
  };

  const handleSortChange = (value: string) => {
    setSortOrder(value);
    setCurrentPage(0);
  };

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
      <div>
        <h2 className="text-heading-lg text-neutral-900">My Applications</h2>
        <p className="text-body-md text-neutral-500 mt-1">
          Track and manage your job applications
        </p>
      </div>

      <Card>
        <CardContent className="p-5">
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="flex-1">
              <Input
                placeholder="Search by job title or location..."
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
            <div className="w-full sm:w-44">
              <Select
                options={statusOptions}
                value={statusFilter}
                onChange={(e) => handleStatusChange(e.target.value)}
                placeholder="Status"
              />
            </div>
            <div className="w-full sm:w-40">
              <Select
                options={sortOptions}
                value={sortOrder}
                onChange={(e) => handleSortChange(e.target.value)}
                placeholder="Sort by"
              />
            </div>
          </div>
        </CardContent>
      </Card>

      {loading ? (
        <div className="space-y-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <ApplicationCardSkeleton key={i} />
          ))}
        </div>
      ) : error ? (
        <Card>
          <CardContent className="p-8 text-center">
            <p className="text-body-md text-error-600 mb-4">{error}</p>
            <Button variant="outline" onClick={() => fetchApplications({ q: searchQuery || undefined, status: statusFilter || undefined, page: currentPage, size: 10, sort: sortOrder })}>
              Try Again
            </Button>
          </CardContent>
        </Card>
      ) : applications.length === 0 ? (
        <Card>
          <CardContent className="p-8">
            <EmptyState
              icon={
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M9 12h3.75M9 15h3.75M9 18h3.75m3 .75H18a2.25 2.25 0 002.25-2.25V6.108c0-1.135-.845-2.098-1.976-2.192a48.424 48.424 0 00-1.123-.08m-5.801 0c-.065.21-.1.433-.1.664 0 .414.336.75.75.75h4.5a.75.75 0 00.75-.75 2.25 2.25 0 00-.1-.664m-5.8 0A2.251 2.251 0 0113.5 2.25H15c1.012 0 1.867.668 2.15 1.586m-5.8 0c-.376.023-.75.05-1.124.08C9.095 4.01 8.25 4.973 8.25 6.108V8.25m0 0H4.875c-.621 0-1.125.504-1.125 1.125v11.25c0 .621.504 1.125 1.125 1.125h9.75c.621 0 1.125-.504 1.125-1.125V9.375c0-.621-.504-1.125-1.125-1.125H8.25z" />
                </svg>
              }
              title="No applications found"
              description={searchQuery || statusFilter ? 'Try adjusting your search or filter criteria.' : 'You haven\'t applied to any jobs yet. Start browsing to find your next opportunity.'}
              action={
                !searchQuery && !statusFilter ? (
                  <Button onClick={() => navigate('/candidate/jobs')}>Browse Jobs</Button>
                ) : undefined
              }
            />
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="flex items-center justify-between text-body-sm text-neutral-500">
            <span>{totalElements} application{totalElements !== 1 ? 's' : ''} found</span>
          </div>
          <div className="space-y-3">
            {applications.map((app) => (
              <ApplicationCard
                key={app.applicationId}
                app={app}
                onClick={() => navigate(`/candidate/applications/${app.applicationId}`)}
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
