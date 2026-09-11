import { useState, useEffect } from 'react';
import { Button, Card, CardContent, Input, Select, Badge, EmptyState, Skeleton } from '@/components/ui';
import { adminService, type AdminJob } from '@/services/admin.service';

const statusOptions = [
  { value: '', label: 'All Status' },
  { value: 'DRAFT', label: 'Draft' },
  { value: 'PUBLISHED', label: 'Published' },
  { value: 'CLOSED', label: 'Closed' },
];

const statusBadgeColors: Record<string, 'default' | 'success' | 'warning' | 'error'> = {
  DRAFT: 'default',
  PUBLISHED: 'success',
  CLOSED: 'error',
};

export default function AdminJobsPage() {
  const [jobs, setJobs] = useState<AdminJob[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [totalElements, setTotalElements] = useState(0);

  const fetchJobs = async (pageNum: number = 0, reset: boolean = false) => {
    try {
      setLoading(true);
      setError(null);
      const response = await adminService.getJobs({
        q: search || undefined,
        status: statusFilter || undefined,
        page: pageNum,
        size: 20,
      });
      if (response.success && response.data) {
        if (reset || pageNum === 0) {
          setJobs(response.data.content);
        } else {
          setJobs(prev => [...prev, ...response.data!.content]);
        }
        setHasMore(!response.data.last);
        setTotalElements(response.data.totalElements);
      }
    } catch (err) {
      setError('Failed to load jobs');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchJobs(0, true);
  }, [search, statusFilter]);

  const handleStatusChange = async (jobId: number, newStatus: string) => {
    if (!confirm(`Are you sure you want to change this job's status to ${newStatus}?`)) return;
    try {
      const response = await adminService.updateJobStatus(jobId, newStatus);
      if (response.success) {
        fetchJobs(0, true);
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update job status');
    }
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-heading-lg text-neutral-900">Job Moderation</h1>
          <p className="text-body-sm text-neutral-500 mt-1">{totalElements} total jobs</p>
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
                placeholder="Search by title or location..."
                value={search}
                onChange={e => setSearch(e.target.value)}
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
              onChange={e => setStatusFilter(e.target.value)}
              className="w-full sm:w-40"
            />
          </div>
        </CardContent>
      </Card>

      {loading && jobs.length === 0 ? (
        <div className="space-y-4">
          {[1, 2, 3, 4, 5].map(i => (
            <Card key={i}>
              <CardContent className="p-4">
                <div className="flex items-center gap-4">
                  <div className="flex-1 space-y-2">
                    <Skeleton className="h-5 w-48" />
                    <Skeleton className="h-4 w-32" />
                  </div>
                  <Skeleton className="h-8 w-20" />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : jobs.length === 0 ? (
        <Card>
          <CardContent className="p-12">
            <EmptyState
              icon={
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                  <rect x="2" y="7" width="20" height="14" rx="2" ry="2" />
                  <path d="M16 21V5a2 2 0 00-2-2h-4a2 2 0 00-2 2v16" />
                </svg>
              }
              title="No jobs found"
              description="Try adjusting your search or filters."
            />
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {jobs.map(job => (
            <Card key={job.id}>
              <CardContent className="p-4">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <h3 className="text-body-lg font-medium text-neutral-900 truncate">{job.title}</h3>
                      <Badge variant={statusBadgeColors[job.status]} size="sm">{job.status}</Badge>
                    </div>
                    <div className="flex flex-wrap gap-4 text-body-sm text-neutral-500">
                      <span>{job.recruiterName}</span>
                      {job.location && <span>{job.location}</span>}
                      {job.workplaceType && <span>{job.workplaceType}</span>}
                      {job.employmentType && <span>{job.employmentType}</span>}
                    </div>
                    <div className="flex gap-4 text-caption text-neutral-400 mt-2">
                      <span>Created {formatDate(job.createdAt)}</span>
                      {job.publishedAt && <span>Published {formatDate(job.publishedAt)}</span>}
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    {job.status !== 'PUBLISHED' && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleStatusChange(job.id, 'PUBLISHED')}
                      >
                        Publish
                      </Button>
                    )}
                    {job.status !== 'CLOSED' && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleStatusChange(job.id, 'CLOSED')}
                        className="text-error-600"
                      >
                        Close
                      </Button>
                    )}
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
                  setPage(prev => prev + 1);
                  fetchJobs(page + 1);
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
