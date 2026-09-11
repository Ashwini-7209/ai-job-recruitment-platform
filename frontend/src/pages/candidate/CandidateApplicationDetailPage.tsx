import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, CardContent, Badge, Button, Skeleton } from '@/components/ui';
import { applicationService } from '@/services/application.service';
import type { CandidateApplicationDetail, StatusHistoryEntry } from '@/types/candidate';

const statusBadgeVariant: Record<string, 'info' | 'warning' | 'success' | 'error' | 'default' | 'primary'> = {
  APPLIED: 'info',
  UNDER_REVIEW: 'warning',
  SHORTLISTED: 'success',
  REJECTED: 'error',
  HIRED: 'primary',
  WITHDRAWN: 'default',
};

const statusLabels: Record<string, string> = {
  APPLIED: 'Applied',
  UNDER_REVIEW: 'Under Review',
  SHORTLISTED: 'Shortlisted',
  REJECTED: 'Rejected',
  HIRED: 'Hired',
  WITHDRAWN: 'Withdrawn',
};

const statusIcons: Record<string, string> = {
  APPLIED: 'M9 12h3.75M9 15h3.75M9 18h3.75m3 .75H18a2.25 2.25 0 002.25-2.25V6.108c0-1.135-.845-2.098-1.976-2.192a48.424 48.424 0 00-1.123-.08m-5.801 0c-.065.21-.1.433-.1.664 0 .414.336.75.75.75h4.5a.75.75 0 00.75-.75 2.25 2.25 0 00-.1-.664m-5.8 0A2.251 2.251 0 0113.5 2.25H15c1.012 0 1.867.668 2.15 1.586m-5.8 0c-.376.023-.75.05-1.124.08C9.095 4.01 8.25 4.973 8.25 6.108V8.25m0 0H4.875c-.621 0-1.125.504-1.125 1.125v11.25c0 .621.504 1.125 1.125 1.125h9.75c.621 0 1.125-.504 1.125-1.125V9.375c0-.621-.504-1.125-1.125-1.125H8.25z',
  UNDER_REVIEW: 'M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z',
  SHORTLISTED: 'M9 12.75L11.25 15 15 9.75M21 12c0 1.268-.63 2.39-1.593 3.068a3.745 3.745 0 01-1.043 3.296 3.745 3.745 0 01-3.296 1.043A3.745 3.745 0 0112 21c-1.268 0-2.39-.63-3.068-1.593a3.746 3.746 0 01-3.296-1.043 3.745 3.745 0 01-1.043-3.296A3.745 3.745 0 013 12c0-1.268.63-2.39 1.593-3.068a3.745 3.745 0 011.043-3.296 3.746 3.746 0 013.296-1.043A3.746 3.746 0 0112 3c1.268 0 2.39.63 3.068 1.593a3.746 3.746 0 013.296 1.043 3.746 3.746 0 011.043 3.296A3.745 3.745 0 0121 12z',
  REJECTED: 'M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636',
  HIRED: 'M4.5 12.75l6 6 9-13.5',
  WITHDRAWN: 'M6 18L18 6M6 6l12 12',
};

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function formatDateTime(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('en-US', {
    month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit',
  });
}

function formatEmploymentType(type: string): string {
  return type.replace('_', ' ').toLowerCase().replace(/\b\w/g, (l) => l.toUpperCase());
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function StatusTimeline({ history, currentStatus }: { history: StatusHistoryEntry[]; currentStatus: string }) {
  const displaySteps = history.length > 0 ? history : [];

  return (
    <div className="space-y-0">
      {displaySteps.map((entry, index) => (
        <div key={entry.id} className="flex gap-3">
          <div className="flex flex-col items-center">
            <div className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-full ${
              index === displaySteps.length - 1 ? 'bg-primary-100 text-primary-600' : 'bg-neutral-100 text-neutral-400'
            }`}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d={statusIcons[entry.newStatus] || statusIcons.APPLIED} />
              </svg>
            </div>
            {index < displaySteps.length - 1 && (
              <div className="w-0.5 h-8 bg-neutral-200" />
            )}
          </div>
          <div className="pb-6 flex-1">
            <div className="flex items-center gap-2">
              <Badge variant={statusBadgeVariant[entry.newStatus]} size="sm">
                {statusLabels[entry.newStatus]}
              </Badge>
              {entry.oldStatus && (
                <span className="text-caption text-neutral-400">from {statusLabels[entry.oldStatus]}</span>
              )}
            </div>
            {entry.changedByName && (
              <p className="text-caption text-neutral-500 mt-1">
                {entry.changedByName} &middot; {formatDateTime(entry.changedAt)}
              </p>
            )}
          </div>
        </div>
      ))}
      {displaySteps.length === 0 && (
        <div className="flex gap-3">
          <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-primary-100 text-primary-600">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d={statusIcons[currentStatus] || statusIcons.APPLIED} />
            </svg>
          </div>
          <div>
            <Badge variant={statusBadgeVariant[currentStatus]} size="sm">
              {statusLabels[currentStatus]}
            </Badge>
          </div>
        </div>
      )}
    </div>
  );
}

export default function CandidateApplicationDetailPage() {
  const { applicationId } = useParams<{ applicationId: string }>();
  const navigate = useNavigate();
  const [application, setApplication] = useState<CandidateApplicationDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [withdrawing, setWithdrawing] = useState(false);

  useEffect(() => {
    if (!applicationId) return;
    const fetchDetail = async () => {
      try {
        setLoading(true);
        setError(null);
        const data = await applicationService.getApplicationDetail(Number(applicationId));
        setApplication(data);
      } catch (err) {
        setError('Failed to load application details.');
      } finally {
        setLoading(false);
      }
    };
    fetchDetail();
  }, [applicationId]);

  const handleWithdraw = async () => {
    if (!application || !window.confirm('Are you sure you want to withdraw this application? This action cannot be undone.')) return;
    try {
      setWithdrawing(true);
      await applicationService.withdrawApplication(application.applicationId);
      setApplication({ ...application, status: 'WITHDRAWN', withdrawnAt: new Date().toISOString() });
    } catch {
      alert('Failed to withdraw application. Please try again.');
    } finally {
      setWithdrawing(false);
    }
  };

  const canWithdraw = application && (application.status === 'APPLIED' || application.status === 'UNDER_REVIEW');

  if (loading) {
    return (
      <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
        <Skeleton className="h-8 w-64" />
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 space-y-6">
            <Card><CardContent className="p-5"><Skeleton className="h-64" /></CardContent></Card>
          </div>
          <div className="space-y-6">
            <Card><CardContent className="p-5"><Skeleton className="h-48" /></CardContent></Card>
          </div>
        </div>
      </div>
    );
  }

  if (error || !application) {
    return (
      <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto">
        <Card>
          <CardContent className="p-8 text-center">
            <p className="text-body-md text-error-600 mb-4">{error || 'Application not found.'}</p>
            <Button variant="outline" onClick={() => navigate('/candidate/applications')}>Back to Applications</Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button onClick={() => navigate('/candidate/applications')} className="text-neutral-400 hover:text-neutral-600 transition-colors">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M15 18l-6-6 6-6" />
          </svg>
        </button>
        <div>
          <h2 className="text-heading-lg text-neutral-900">{application.jobTitle}</h2>
          <p className="text-body-sm text-neutral-500 mt-0.5">Application Details</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardContent className="p-5 space-y-4">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <h3 className="text-heading-sm text-neutral-900">{application.jobTitle}</h3>
                  <div className="flex flex-wrap items-center gap-x-3 gap-y-1 mt-2 text-body-sm text-neutral-500">
                    <span className="flex items-center gap-1">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z" />
                        <circle cx="12" cy="10" r="3" />
                      </svg>
                      {application.location || 'Remote'}
                    </span>
                    <span>{formatEmploymentType(application.employmentType)}</span>
                    <span>{application.workplaceType}</span>
                  </div>
                </div>
                <Badge variant={statusBadgeVariant[application.status]}>
                  {statusLabels[application.status]}
                </Badge>
              </div>

              {application.jobDescription && (
                <div>
                  <h4 className="text-label-lg text-neutral-700 mb-1">Job Description</h4>
                  <p className="text-body-sm text-neutral-600 whitespace-pre-wrap">{application.jobDescription}</p>
                </div>
              )}

              {(application.salaryMin || application.salaryMax) && (
                <div className="flex items-center gap-2 text-body-sm text-neutral-600">
                  <span className="font-medium">Salary:</span>
                  <span>{application.salaryMin && application.salaryMax
                    ? `$${application.salaryMin.toLocaleString()} - $${application.salaryMax.toLocaleString()}`
                    : application.salaryMin ? `From $${application.salaryMin.toLocaleString()}`
                    : `Up to $${application.salaryMax!.toLocaleString()}`}
                  </span>
                </div>
              )}

              {application.skills && (
                <div>
                  <h4 className="text-label-lg text-neutral-700 mb-1">Required Skills</h4>
                  <div className="flex flex-wrap gap-1.5">
                    {application.skills.split(',').map((skill, i) => (
                      <span key={i} className="inline-flex items-center rounded-full bg-neutral-100 px-2.5 py-0.5 text-xs font-medium text-neutral-700">
                        {skill.trim()}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </CardContent>
          </Card>

          {application.coverLetter && (
            <Card>
              <CardContent className="p-5">
                <h4 className="text-label-lg text-neutral-700 mb-2">Your Cover Letter</h4>
                <p className="text-body-sm text-neutral-600 whitespace-pre-wrap">{application.coverLetter}</p>
              </CardContent>
            </Card>
          )}

          <Card>
            <CardContent className="p-5">
              <h4 className="text-label-lg text-neutral-700 mb-3">Application Timeline</h4>
              <StatusTimeline history={application.statusHistory} currentStatus={application.status} />
            </CardContent>
          </Card>
        </div>

        <div className="space-y-6">
          <Card>
            <CardContent className="p-5 space-y-4">
              <h4 className="text-label-lg text-neutral-700">Application Info</h4>
              <div className="space-y-3 text-body-sm">
                <div className="flex justify-between">
                  <span className="text-neutral-500">Applied</span>
                  <span className="text-neutral-900 font-medium">{formatDate(application.appliedAt)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-neutral-500">Last Updated</span>
                  <span className="text-neutral-900 font-medium">{formatDate(application.updatedAt)}</span>
                </div>
                {application.deadline && (
                  <div className="flex justify-between">
                    <span className="text-neutral-500">Deadline</span>
                    <span className="text-neutral-900 font-medium">{formatDate(application.deadline)}</span>
                  </div>
                )}
                {application.withdrawnAt && (
                  <div className="flex justify-between">
                    <span className="text-neutral-500">Withdrawn</span>
                    <span className="text-neutral-900 font-medium">{formatDate(application.withdrawnAt)}</span>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>

          {application.resumeFileName && (
            <Card>
              <CardContent className="p-5 space-y-3">
                <h4 className="text-label-lg text-neutral-700">Submitted Resume</h4>
                <div className="flex items-center gap-3 rounded-lg border border-neutral-200 p-3">
                  <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-primary-50 text-primary-600">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
                    </svg>
                  </div>
                  <div className="min-w-0 flex-1">
                    <p className="text-body-sm font-medium text-neutral-900 truncate">{application.resumeFileName}</p>
                    {application.resumeFileSize && (
                      <p className="text-caption text-neutral-500">{formatFileSize(application.resumeFileSize)}</p>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>
          )}

          {canWithdraw && (
            <Card>
              <CardContent className="p-5">
                <Button
                  variant="danger"
                  fullWidth
                  onClick={handleWithdraw}
                  disabled={withdrawing}
                >
                  {withdrawing ? 'Withdrawing...' : 'Withdraw Application'}
                </Button>
                <p className="text-caption text-neutral-500 mt-2 text-center">
                  You can withdraw if status is Applied or Under Review
                </p>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}
