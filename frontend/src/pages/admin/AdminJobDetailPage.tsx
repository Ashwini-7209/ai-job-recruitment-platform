import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Button, Card, CardContent, Badge, Skeleton } from '@/components/ui';
import { adminService, type AdminJob } from '@/services/admin.service';
import { api } from '@/services/api';

const statusBadgeColors: Record<string, 'default' | 'success' | 'error'> = {
  DRAFT: 'default',
  PUBLISHED: 'success',
  CLOSED: 'error',
};

export default function AdminJobDetailPage() {
  const { jobId } = useParams<{ jobId: string }>();
  const navigate = useNavigate();
  const [job, setJob] = useState<AdminJob | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const fetchJob = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await api.get<AdminJob>(`/admin/jobs/${jobId}`);
      if (response.success && response.data) {
        setJob(response.data);
      } else {
        setError('Job not found');
      }
    } catch (err) {
      setError('Failed to load job details');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (jobId) {
      fetchJob();
    }
  }, [jobId]);

  const handleStatusChange = async (newStatus: string) => {
    if (!job) return;
    if (!confirm(`Are you sure you want to change this job's status to ${newStatus}?`)) return;

    try {
      setActionLoading(true);
      setSuccessMessage(null);
      const response = await adminService.updateJobStatus(job.id, newStatus);
      if (response.success && response.data) {
        setJob(response.data);
        setSuccessMessage(`Job status updated to ${newStatus}`);
        setTimeout(() => setSuccessMessage(null), 3000);
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update job status');
    } finally {
      setActionLoading(false);
    }
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatExperience = (min?: number | null, max?: number | null) => {
    if (min == null && max == null) return '-';
    if (min != null && max != null) return `${min} - ${max} years`;
    if (min != null) return `${min}+ years`;
    return `Up to ${max} years`;
  };

  if (loading) {
    return (
      <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
        <Skeleton className="h-8 w-48" />
        <Card>
          <CardContent className="space-y-6">
            <div className="space-y-3">
              <Skeleton className="h-8 w-64" />
              <Skeleton className="h-5 w-24" />
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Skeleton className="h-16 w-full" />
              <Skeleton className="h-16 w-full" />
              <Skeleton className="h-16 w-full" />
              <Skeleton className="h-16 w-full" />
            </div>
            <Skeleton className="h-24 w-full" />
          </CardContent>
        </Card>
      </div>
    );
  }

  if (error || !job) {
    return (
      <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
        <Button variant="ghost" onClick={() => navigate('/admin/jobs')}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M19 12H5M12 19l-7-7 7-7" />
          </svg>
          Back to Jobs
        </Button>
        <div className="rounded-lg bg-error-50 p-4 text-body-sm text-error-700">
          {error || 'Job not found'}
        </div>
      </div>
    );
  }

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
      <Button variant="ghost" onClick={() => navigate('/admin/jobs')}>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M19 12H5M12 19l-7-7 7-7" />
        </svg>
        Back to Jobs
      </Button>

      {successMessage && (
        <div className="rounded-lg bg-success-50 p-4 text-body-sm text-success-700">{successMessage}</div>
      )}

      <Card>
        <CardContent className="space-y-6">
          <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
            <div>
              <h1 className="text-heading-lg text-neutral-900">{job.title}</h1>
              <div className="flex items-center gap-2 mt-2">
                <Badge variant={statusBadgeColors[job.status]}>{job.status}</Badge>
                <span className="text-body-sm text-neutral-500">{job.applicationCount ?? 0} applications</span>
              </div>
            </div>
            <div className="flex items-center gap-2">
              {job.status !== 'PUBLISHED' && (
                <Button
                  variant="outline"
                  loading={actionLoading}
                  onClick={() => handleStatusChange('PUBLISHED')}
                >
                  Publish
                </Button>
              )}
              {job.status !== 'CLOSED' && (
                <Button
                  variant="ghost"
                  loading={actionLoading}
                  onClick={() => handleStatusChange('CLOSED')}
                  className="text-error-600 hover:bg-error-50"
                >
                  Close
                </Button>
              )}
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
            <div className="space-y-1">
              <p className="text-caption text-neutral-400 uppercase tracking-wide">Recruiter</p>
              <p className="text-body-md text-neutral-900">{job.recruiterName}</p>
              <p className="text-body-sm text-neutral-500">{job.recruiterEmail}</p>
            </div>
            <div className="space-y-1">
              <p className="text-caption text-neutral-400 uppercase tracking-wide">Location</p>
              <p className="text-body-md text-neutral-900">{job.location || '-'}</p>
            </div>
            <div className="space-y-1">
              <p className="text-caption text-neutral-400 uppercase tracking-wide">Workplace Type</p>
              <p className="text-body-md text-neutral-900">{job.workplaceType || '-'}</p>
            </div>
            <div className="space-y-1">
              <p className="text-caption text-neutral-400 uppercase tracking-wide">Employment Type</p>
              <p className="text-body-md text-neutral-900">{job.employmentType || '-'}</p>
            </div>
            <div className="space-y-1">
              <p className="text-caption text-neutral-400 uppercase tracking-wide">Experience</p>
              <p className="text-body-md text-neutral-900">{formatExperience(job.experienceMin, job.experienceMax)}</p>
            </div>
            <div className="space-y-1">
              <p className="text-caption text-neutral-400 uppercase tracking-wide">Applications</p>
              <p className="text-body-md text-neutral-900">{job.applicationCount ?? 0}</p>
            </div>
          </div>

          <div className="border-t border-neutral-100 pt-6">
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div className="space-y-1">
                <p className="text-caption text-neutral-400 uppercase tracking-wide">Created</p>
                <p className="text-body-sm text-neutral-700">{formatDate(job.createdAt)}</p>
              </div>
              <div className="space-y-1">
                <p className="text-caption text-neutral-400 uppercase tracking-wide">Published</p>
                <p className="text-body-sm text-neutral-700">{formatDate(job.publishedAt)}</p>
              </div>
              <div className="space-y-1">
                <p className="text-caption text-neutral-400 uppercase tracking-wide">Deadline</p>
                <p className="text-body-sm text-neutral-700">{formatDate(job.applicationDeadline)}</p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
