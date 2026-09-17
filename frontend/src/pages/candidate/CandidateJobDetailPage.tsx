import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, CardContent, Button, Badge, Skeleton, EmptyState } from '@/components/ui';
import { jobService, type JobListItem } from '@/services/job.service';
import { savedJobService } from '@/services/savedJob.service';
import { resumeService, type Resume } from '@/services/resume.service';
import { aiService, type JobMatchResponse } from '@/services/ai.service';
import { api } from '@/services/api';

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

function formatEmploymentType(type: string): string {
  if (!type) return '';
  return type.replace('_', ' ').toLowerCase().replace(/\b\w/g, (l) => l.toUpperCase());
}

function formatWorkplaceType(type: string): string {
  if (!type) return '';
  return type.replace('_', ' ').toLowerCase().replace(/\b\w/g, (l) => l.toUpperCase());
}

function formatExperienceRange(min: number | null, max: number | null): string | null {
  if (!min && !max) return null;
  if (min && max) return `${min} - ${max} years`;
  if (min) return `${min}+ years`;
  return `Up to ${max} years`;
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function getDaysUntilDeadline(deadline: string): number {
  const now = new Date();
  const deadlineDate = new Date(deadline);
  const diffTime = deadlineDate.getTime() - now.getTime();
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
}

export default function CandidateJobDetailPage() {
  const { jobId } = useParams<{ jobId: string }>();
  const navigate = useNavigate();
  const [job, setJob] = useState<JobListItem | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [applying, setApplying] = useState(false);
  const [showApplyModal, setShowApplyModal] = useState(false);
  const [coverLetter, setCoverLetter] = useState('');
  const [applySuccess, setApplySuccess] = useState(false);
  const [applyError, setApplyError] = useState<string | null>(null);
  const [resumes, setResumes] = useState<Resume[]>([]);
  const [selectedResumeId, setSelectedResumeId] = useState<number | null>(null);
  const [matchData, setMatchData] = useState<JobMatchResponse | null>(null);
  const [matchLoading, setMatchLoading] = useState(false);
  const [matchError, setMatchError] = useState<string | null>(null);
  const [matchExpanded, setMatchExpanded] = useState(true);

  const fetchJob = useCallback(async () => {
    if (!jobId) return;
    try {
      setIsLoading(true);
      setError(null);
      const numericJobId = Number(jobId);
      if (isNaN(numericJobId)) {
        setError('Invalid job ID.');
        return;
      }
      const data = await jobService.getJobById(numericJobId);
      setJob(data);
    } catch {
      setError('Failed to load job details. Please try again.');
    } finally {
      setIsLoading(false);
    }
  }, [jobId]);

  const fetchResumes = useCallback(async () => {
    try {
      const data = await resumeService.getResumes();
      setResumes(data);
      const activeResume = data.find((r) => r.active);
      if (activeResume) {
        setSelectedResumeId(activeResume.id);
      }
    } catch {
      // Resume fetch failed silently
    }
  }, []);

  const fetchMatch = useCallback(async () => {
    if (!jobId) return;
    const numericJobId = Number(jobId);
    if (isNaN(numericJobId)) return;
    try {
      setMatchLoading(true);
      setMatchError(null);
      const response = await aiService.getJobMatch(numericJobId);
      setMatchData(response.data ?? null);
    } catch {
      setMatchError('Could not load match analysis.');
    } finally {
      setMatchLoading(false);
    }
  }, [jobId]);

  useEffect(() => {
    fetchJob();
    fetchResumes();
    fetchMatch();
  }, [fetchJob, fetchResumes, fetchMatch]);

  const handleToggleSave = async () => {
    if (!job || saving) return;
    setSaving(true);
    try {
      if (job.saved) {
        await savedJobService.unsaveJob(job.jobId);
        setJob({ ...job, saved: false });
      } else {
        await savedJobService.saveJob(job.jobId);
        setJob({ ...job, saved: true });
      }
    } catch {
      // silently fail
    } finally {
      setSaving(false);
    }
  };

  const handleApply = async () => {
    if (!job || applying) return;
    setApplying(true);
    setApplyError(null);
    try {
      await api.post(`/jobs/${job.jobId}/applications`, {
        coverLetter: coverLetter.trim() || undefined,
        resumeId: selectedResumeId || undefined,
      });
      setJob({ ...job, applied: true });
      setApplySuccess(true);
      setShowApplyModal(false);
      setTimeout(() => setApplySuccess(false), 3000);
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } };
      setApplyError(axiosError.response?.data?.message || 'Failed to apply. Please try again.');
    } finally {
      setApplying(false);
    }
  };

  const openApplyModal = () => {
    setCoverLetter('');
    setApplyError(null);
    setShowApplyModal(true);
  };

  if (isLoading) {
    return (
      <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
        <div className="flex items-center gap-3">
          <Skeleton className="h-10 w-10 rounded-lg" />
          <div className="space-y-2">
            <Skeleton className="h-7 w-64" />
            <Skeleton className="h-4 w-40" />
          </div>
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 space-y-6">
            <Card>
              <CardContent className="p-5 space-y-4">
                <Skeleton className="h-6 w-48" />
                <Skeleton className="h-4 w-32" />
                <div className="flex gap-2">
                  <Skeleton className="h-6 w-20 rounded-full" />
                  <Skeleton className="h-6 w-24 rounded-full" />
                </div>
                <Skeleton className="h-40 w-full" />
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-5 space-y-3">
                <Skeleton className="h-5 w-32" />
                <div className="flex flex-wrap gap-2">
                  <Skeleton className="h-6 w-16 rounded-full" />
                  <Skeleton className="h-6 w-20 rounded-full" />
                  <Skeleton className="h-6 w-14 rounded-full" />
                </div>
              </CardContent>
            </Card>
          </div>
          <div className="space-y-6">
            <Card>
              <CardContent className="p-5 space-y-4">
                <Skeleton className="h-5 w-32" />
                <Skeleton className="h-10 w-full" />
                <Skeleton className="h-10 w-full" />
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    );
  }

  if (error || !job) {
    return (
      <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto">
        <Card>
          <CardContent className="p-8 text-center">
            <EmptyState
              icon={
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z" />
                </svg>
              }
              title={error || 'Job not found'}
              description="The job you're looking for doesn't exist or has been removed."
              action={
                <Button variant="outline" onClick={() => navigate('/candidate/jobs')}>
                  Back to Jobs
                </Button>
              }
            />
          </CardContent>
        </Card>
      </div>
    );
  }

  const deadlineInfo = job.applicationDeadline ? getDaysUntilDeadline(job.applicationDeadline) : null;

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/candidate/jobs')}
          className="flex h-10 w-10 items-center justify-center rounded-lg border border-neutral-200 text-neutral-500 hover:bg-neutral-50 hover:text-neutral-700 transition-colors"
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M15 18l-6-6 6-6" />
          </svg>
        </button>
        <div className="min-w-0 flex-1">
          <h1 className="text-heading-lg text-neutral-900 truncate">{job.title}</h1>
          <p className="text-body-sm text-neutral-500 mt-0.5">{job.companyName}</p>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          <Button
            variant="ghost"
            size="sm"
            onClick={handleToggleSave}
            disabled={saving}
            className={job.saved ? 'text-error-500 hover:text-error-600 hover:bg-error-50' : 'text-neutral-400 hover:text-error-500'}
          >
            {saving ? (
              <div className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
            ) : (
              <svg width="18" height="18" viewBox="0 0 24 24" fill={job.saved ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2z" />
              </svg>
            )}
          </Button>
        </div>
      </div>

      {applySuccess && (
        <div className="flex items-center gap-2 rounded-lg bg-success-50 border border-success-200 p-4 text-body-sm text-success-700">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M9 12.75L11.25 15 15 9.75M21 12c0 1.268-.63 2.39-1.593 3.068a3.745 3.745 0 01-1.043 3.296 3.745 3.745 0 01-3.296 1.043A3.745 3.745 0 0112 21c-1.268 0-2.39-.63-3.068-1.593a3.746 3.746 0 01-3.296-1.043 3.745 3.745 0 01-1.043-3.296A3.745 3.745 0 013 12c0-1.268.63-2.39 1.593-3.068a3.745 3.745 0 011.043-3.296 3.746 3.746 0 013.296-1.043A3.746 3.746 0 0112 3c1.268 0 2.39.63 3.068 1.593a3.746 3.746 0 013.296 1.043 3.746 3.746 0 011.043 3.296A3.745 3.745 0 0121 12z" />
          </svg>
          Application submitted successfully!
        </div>
      )}

      {!matchError && (matchLoading || matchData) && (
        <Card>
          <button
            type="button"
            onClick={() => setMatchExpanded(!matchExpanded)}
            className="w-full flex items-center justify-between p-5 text-left hover:bg-neutral-50 transition-colors rounded-xl"
          >
            <div className="flex items-center gap-3">
              <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-secondary-50 text-secondary-600">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M12 2a10 10 0 100 20 10 10 0 000-20z" />
                  <path d="M8 12.5l2.5 2.5 5.5-5.5" />
                </svg>
              </div>
              <div>
                <h3 className="text-heading-sm text-neutral-900">AI Match Analysis</h3>
                <p className="text-caption text-neutral-500">How your profile matches this role</p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              {matchData && (
                <Badge variant={matchData.aiUsed ? 'secondary' : 'default'} size="sm">
                  {matchData.aiUsed ? 'AI-enhanced' : 'Calculated'}
                </Badge>
              )}
              <svg
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                className={`text-neutral-400 transition-transform ${matchExpanded ? 'rotate-180' : ''}`}
              >
                <polyline points="6 9 12 15 18 9" />
              </svg>
            </div>
          </button>

          {matchExpanded && (
            <CardContent className="px-5 pb-5 pt-0">
              {matchLoading && (
                <div className="space-y-4 pt-2">
                  <div className="flex items-center gap-6">
                    <Skeleton className="h-24 w-24 rounded-full" />
                    <div className="flex-1 space-y-2">
                      <Skeleton className="h-4 w-40" />
                      <Skeleton className="h-3 w-56" />
                      <Skeleton className="h-3 w-48" />
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <Skeleton className="h-6 w-20 rounded-full" />
                    <Skeleton className="h-6 w-24 rounded-full" />
                    <Skeleton className="h-6 w-16 rounded-full" />
                  </div>
                </div>
              )}

              {!matchLoading && matchData && (
                <div className="space-y-5 pt-2">
                  <div className="flex items-start gap-6">
                    <div className="relative flex-shrink-0">
                      <svg width="100" height="100" viewBox="0 0 100 100" className="-rotate-90">
                        <circle cx="50" cy="50" r="42" fill="none" stroke="currentColor" strokeWidth="6" className="text-neutral-100" />
                        <circle
                          cx="50"
                          cy="50"
                          r="42"
                          fill="none"
                          strokeWidth="6"
                          strokeLinecap="round"
                          strokeDasharray={`${2 * Math.PI * 42}`}
                          strokeDashoffset={`${2 * Math.PI * 42 * (1 - matchData.overallScore / 100)}`}
                          className={
                            matchData.overallScore >= 80 ? 'stroke-success-500' :
                            matchData.overallScore >= 60 ? 'stroke-warning-500' :
                            'stroke-error-500'
                          }
                        />
                      </svg>
                      <div className="absolute inset-0 flex flex-col items-center justify-center">
                        <span className={`text-2xl font-bold ${
                          matchData.overallScore >= 80 ? 'text-success-600' :
                          matchData.overallScore >= 60 ? 'text-warning-600' :
                          'text-error-600'
                        }`}>
                          {matchData.overallScore}%
                        </span>
                      </div>
                    </div>
                    <div className="flex-1 min-w-0 space-y-2">
                      <p className="text-body-sm text-neutral-700 font-medium">
                        Overall Match: {matchData.jobTitle}
                      </p>
                      <div className="space-y-1.5">
                        <div className="flex items-center gap-2 text-caption text-neutral-500">
                          <span className="w-20 shrink-0">Skills</span>
                          <div className="flex-1 h-1.5 rounded-full bg-neutral-100 overflow-hidden">
                            <div
                              className="h-full rounded-full bg-secondary-400"
                              style={{ width: `${matchData.skillScore}%` }}
                            />
                          </div>
                          <span className="w-8 text-right text-neutral-600 font-medium">{matchData.skillScore}%</span>
                        </div>
                        <div className="flex items-center gap-2 text-caption text-neutral-500">
                          <span className="w-20 shrink-0">Experience</span>
                          <div className="flex-1 h-1.5 rounded-full bg-neutral-100 overflow-hidden">
                            <div
                              className="h-full rounded-full bg-secondary-400"
                              style={{ width: `${matchData.experienceScore}%` }}
                            />
                          </div>
                          <span className="w-8 text-right text-neutral-600 font-medium">{matchData.experienceScore}%</span>
                        </div>
                        <div className="flex items-center gap-2 text-caption text-neutral-500">
                          <span className="w-20 shrink-0">Profile</span>
                          <div className="flex-1 h-1.5 rounded-full bg-neutral-100 overflow-hidden">
                            <div
                              className="h-full rounded-full bg-secondary-400"
                              style={{ width: `${matchData.profileScore}%` }}
                            />
                          </div>
                          <span className="w-8 text-right text-neutral-600 font-medium">{matchData.profileScore}%</span>
                        </div>
                      </div>
                    </div>
                  </div>

                  {matchData.matchedSkills.length > 0 && (
                    <div>
                      <p className="text-caption font-medium text-neutral-600 mb-2">Matched Skills</p>
                      <div className="flex flex-wrap gap-1.5">
                        {matchData.matchedSkills.map((skill, i) => (
                          <span
                            key={i}
                            className="inline-flex items-center rounded-full bg-success-50 px-2.5 py-0.5 text-xs font-medium text-success-700 border border-success-200"
                          >
                            {skill}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}

                  {matchData.missingSkills.length > 0 && (
                    <div>
                      <p className="text-caption font-medium text-neutral-600 mb-2">Missing Skills</p>
                      <div className="flex flex-wrap gap-1.5">
                        {matchData.missingSkills.map((skill, i) => (
                          <span
                            key={i}
                            className="inline-flex items-center rounded-full bg-error-50 px-2.5 py-0.5 text-xs font-medium text-error-600 border border-error-200"
                          >
                            {skill}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}

                  {matchData.matchingReasons.length > 0 && (
                    <div>
                      <p className="text-caption font-medium text-neutral-600 mb-2">Why you match</p>
                      <ul className="space-y-1.5">
                        {matchData.matchingReasons.map((reason, i) => (
                          <li key={i} className="flex items-start gap-2 text-caption text-neutral-600">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-success-500 mt-0.5 flex-shrink-0">
                              <path d="M9 12.75L11.25 15 15 9.75M21 12c0 1.268-.63 2.39-1.593 3.068a3.745 3.745 0 01-1.043 3.296 3.745 3.745 0 01-3.296 1.043A3.745 3.745 0 0112 21c-1.268 0-2.39-.63-3.068-1.593a3.746 3.746 0 01-3.296-1.043 3.745 3.745 0 01-1.043-3.296A3.745 3.745 0 013 12c0-1.268.63-2.39 1.593-3.068a3.745 3.745 0 011.043-3.296 3.746 3.746 0 013.296-1.043A3.746 3.746 0 0112 3c1.268 0 2.39.63 3.068 1.593a3.746 3.746 0 013.296 1.043 3.746 3.746 0 011.043 3.296A3.745 3.745 0 0121 12z" />
                            </svg>
                            {reason}
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}

                  {matchData.potentialGaps.length > 0 && (
                    <div>
                      <p className="text-caption font-medium text-neutral-600 mb-2">Potential gaps</p>
                      <ul className="space-y-1.5">
                        {matchData.potentialGaps.map((gap, i) => (
                          <li key={i} className="flex items-start gap-2 text-caption text-neutral-600">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-warning-500 mt-0.5 flex-shrink-0">
                              <path d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z" />
                            </svg>
                            {gap}
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}
                </div>
              )}

              {!matchLoading && !matchData && (
                <div className="py-6 text-center">
                  <p className="text-body-sm text-neutral-500">No profile data available for match analysis.</p>
                </div>
              )}
            </CardContent>
          )}
        </Card>
      )}

      {!matchLoading && matchError && (
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center gap-2 text-body-sm text-neutral-500">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" className="text-neutral-400">
                <path d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z" />
              </svg>
              AI match analysis unavailable
            </div>
          </CardContent>
        </Card>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardContent className="p-5 space-y-5">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <h2 className="text-heading-md text-neutral-900">{job.title}</h2>
                  <div className="flex flex-wrap items-center gap-x-3 gap-y-1 mt-2 text-body-sm text-neutral-500">
                    <span className="flex items-center gap-1">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z" />
                        <circle cx="12" cy="10" r="3" />
                      </svg>
                      {job.location}
                    </span>
                    <span>{formatEmploymentType(job.employmentType)}</span>
                    <span>{formatWorkplaceType(job.workplaceType)}</span>
                  </div>
                </div>
                {job.applied && (
                  <Badge variant="success" size="md">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M9 12.75L11.25 15 15 9.75M21 12c0 1.268-.63 2.39-1.593 3.068a3.745 3.745 0 01-1.043 3.296 3.745 3.745 0 01-3.296 1.043A3.745 3.745 0 0112 21c-1.268 0-2.39-.63-3.068-1.593a3.746 3.746 0 01-3.296-1.043 3.745 3.745 0 01-1.043-3.296A3.745 3.745 0 013 12c0-1.268.63-2.39 1.593-3.068a3.745 3.745 0 011.043-3.296 3.746 3.746 0 013.296-1.043A3.746 3.746 0 0112 3c1.268 0 2.39.63 3.068 1.593a3.746 3.746 0 013.296 1.043 3.746 3.746 0 011.043 3.296A3.745 3.745 0 0121 12z" />
                    </svg>
                    Applied
                  </Badge>
                )}
              </div>

              {(formatSalary(job.salaryMin, job.salaryMax) || formatExperienceRange(job.experienceMin, job.experienceMax)) && (
                <div className="flex flex-wrap items-center gap-4 text-body-sm">
                  {formatSalary(job.salaryMin, job.salaryMax) && (
                    <div className="flex items-center gap-1.5 text-neutral-700">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <line x1="12" y1="1" x2="12" y2="23" />
                        <path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6" />
                      </svg>
                      <span className="font-medium text-neutral-900">{formatSalary(job.salaryMin, job.salaryMax)}</span>
                    </div>
                  )}
                  {formatExperienceRange(job.experienceMin, job.experienceMax) && (
                    <div className="flex items-center gap-1.5 text-neutral-600">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <circle cx="12" cy="12" r="10" />
                        <polyline points="12,6 12,12 16,14" />
                      </svg>
                      <span>{formatExperienceRange(job.experienceMin, job.experienceMax)} experience</span>
                    </div>
                  )}
                </div>
              )}

              <div className="flex flex-wrap items-center gap-3 text-caption text-neutral-500">
                <span className="flex items-center gap-1">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
                    <line x1="16" y1="2" x2="16" y2="6" />
                    <line x1="8" y1="2" x2="8" y2="6" />
                    <line x1="3" y1="10" x2="21" y2="10" />
                  </svg>
                  Posted {formatDate(job.createdAt)}
                </span>
                {job.applicationDeadline && (
                  <span className="flex items-center gap-1">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <circle cx="12" cy="12" r="10" />
                      <polyline points="12,6 12,12 16,14" />
                    </svg>
                    Deadline: {formatDate(job.applicationDeadline)}
                    {deadlineInfo !== null && deadlineInfo > 0 && (
                      <span className={deadlineInfo <= 7 ? 'text-warning-600 font-medium' : ''}>
                        ({deadlineInfo} day{deadlineInfo !== 1 ? 's' : ''} left)
                      </span>
                    )}
                    {deadlineInfo !== null && deadlineInfo <= 0 && (
                      <span className="text-error-600 font-medium">(Expired)</span>
                    )}
                  </span>
                )}
              </div>
            </CardContent>
          </Card>

          {job.skills && (
            <Card>
              <CardContent className="p-5">
                <h3 className="text-label-lg text-neutral-700 mb-3">Required Skills</h3>
                <div className="flex flex-wrap gap-2">
                  {job.skills.split(',').map((skill, i) => (
                    <span
                      key={i}
                      className="inline-flex items-center rounded-full bg-secondary-50 px-3 py-1 text-xs font-medium text-secondary-700"
                    >
                      {skill.trim()}
                    </span>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}

          {job.description && (
            <Card>
              <CardContent className="p-5">
                <h3 className="text-label-lg text-neutral-700 mb-3">Job Description</h3>
                <div className="prose prose-sm max-w-none text-body-sm text-neutral-600 whitespace-pre-wrap leading-relaxed">
                  {job.description}
                </div>
              </CardContent>
            </Card>
          )}
        </div>

        <div className="space-y-6">
          <Card>
            <CardContent className="p-5 space-y-4">
              <h3 className="text-label-lg text-neutral-700">Quick Info</h3>
              <div className="space-y-3 text-body-sm">
                <div className="flex justify-between">
                  <span className="text-neutral-500">Company</span>
                  <span className="text-neutral-900 font-medium">{job.companyName}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-neutral-500">Location</span>
                  <span className="text-neutral-900 font-medium">{job.location}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-neutral-500">Job Type</span>
                  <span className="text-neutral-900 font-medium">{formatEmploymentType(job.employmentType)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-neutral-500">Workplace</span>
                  <span className="text-neutral-900 font-medium">{formatWorkplaceType(job.workplaceType)}</span>
                </div>
                {formatSalary(job.salaryMin, job.salaryMax) && (
                  <div className="flex justify-between">
                    <span className="text-neutral-500">Salary</span>
                    <span className="text-neutral-900 font-medium">{formatSalary(job.salaryMin, job.salaryMax)}</span>
                  </div>
                )}
                {formatExperienceRange(job.experienceMin, job.experienceMax) && (
                  <div className="flex justify-between">
                    <span className="text-neutral-500">Experience</span>
                    <span className="text-neutral-900 font-medium">{formatExperienceRange(job.experienceMin, job.experienceMax)}</span>
                  </div>
                )}
                <div className="flex justify-between">
                  <span className="text-neutral-500">Posted</span>
                  <span className="text-neutral-900 font-medium">{formatDate(job.createdAt)}</span>
                </div>
                {job.applicationDeadline && (
                  <div className="flex justify-between">
                    <span className="text-neutral-500">Deadline</span>
                    <span className="text-neutral-900 font-medium">{formatDate(job.applicationDeadline)}</span>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-5 space-y-3">
              {job.applied ? (
                <div className="text-center">
                  <div className="inline-flex items-center justify-center h-12 w-12 rounded-full bg-success-50 text-success-600 mb-2">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M9 12.75L11.25 15 15 9.75M21 12c0 1.268-.63 2.39-1.593 3.068a3.745 3.745 0 01-1.043 3.296 3.745 3.745 0 01-3.296 1.043A3.745 3.745 0 0112 21c-1.268 0-2.39-.63-3.068-1.593a3.746 3.746 0 01-3.296-1.043 3.745 3.745 0 01-1.043-3.296A3.745 3.745 0 013 12c0-1.268.63-2.39 1.593-3.068a3.745 3.745 0 011.043-3.296 3.746 3.746 0 013.296-1.043A3.746 3.746 0 0112 3c1.268 0 2.39.63 3.068 1.593a3.746 3.746 0 013.296 1.043 3.746 3.746 0 011.043 3.296A3.745 3.745 0 0121 12z" />
                    </svg>
                  </div>
                  <p className="text-heading-sm text-neutral-900">Applied</p>
                  <p className="text-body-sm text-neutral-500 mt-1">You've already applied to this job</p>
                  <Button
                    variant="outline"
                    fullWidth
                    className="mt-3"
                    onClick={() => navigate('/candidate/applications')}
                  >
                    View Application
                  </Button>
                </div>
              ) : (
                <>
                  <Button
                    variant="primary"
                    fullWidth
                    onClick={openApplyModal}
                    disabled={deadlineInfo !== null && deadlineInfo <= 0}
                  >
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z" />
                      <line x1="3" y1="6" x2="21" y2="6" />
                      <path d="M16 10a4 4 0 01-8 0" />
                    </svg>
                    {deadlineInfo !== null && deadlineInfo <= 0 ? 'Applications Closed' : 'Apply Now'}
                  </Button>
                  <Button
                    variant="outline"
                    fullWidth
                    onClick={handleToggleSave}
                    disabled={saving}
                  >
                    {saving ? (
                      <div className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
                    ) : (
                      <svg width="16" height="16" viewBox="0 0 24 24" fill={job.saved ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2z" />
                      </svg>
                    )}
                    {job.saved ? 'Saved' : 'Save Job'}
                  </Button>
                </>
              )}
            </CardContent>
          </Card>

          {job.status !== 'PUBLISHED' && (
            <Card>
              <CardContent className="p-5">
                <Badge variant={job.status === 'CLOSED' ? 'error' : 'warning'} size="lg">
                  {job.status === 'CLOSED' ? 'This job is closed' : 'Draft'}
                </Badge>
              </CardContent>
            </Card>
          )}
        </div>
      </div>

      {showApplyModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div
            className="fixed inset-0 bg-neutral-900/50 backdrop-blur-sm"
            onClick={() => setShowApplyModal(false)}
          />
          <div className="relative z-10 w-full max-w-lg mx-4">
            <Card>
              <CardContent className="p-6">
                <div className="flex items-center justify-between mb-5">
                  <h3 className="text-heading-md text-neutral-900">Apply for {job.title}</h3>
                  <button
                    onClick={() => setShowApplyModal(false)}
                    className="text-neutral-400 hover:text-neutral-600 transition-colors"
                  >
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <line x1="18" y1="6" x2="6" y2="18" />
                      <line x1="6" y1="6" x2="18" y2="18" />
                    </svg>
                  </button>
                </div>

                <div className="space-y-4">
                  {resumes.length > 0 && (
                    <div>
                      <label className="block text-label-md text-neutral-700 mb-1.5">
                        Attach Resume
                      </label>
                      <select
                        value={selectedResumeId || ''}
                        onChange={(e) => setSelectedResumeId(e.target.value ? Number(e.target.value) : null)}
                        className="block w-full rounded-lg border border-neutral-300 bg-white px-3.5 py-2 text-sm text-neutral-900 focus:outline-none focus:ring-2 focus:ring-secondary-500/20 focus:border-secondary-500"
                      >
                        <option value="">No resume attached</option>
                        {resumes.map((resume) => (
                          <option key={resume.id} value={resume.id}>
                            {resume.originalFileName} ({formatFileSize(resume.fileSize)}){resume.active ? ' - Active' : ''}
                          </option>
                        ))}
                      </select>
                      <p className="mt-1 text-caption text-neutral-500">
                        Your active resume will be automatically attached if available.
                      </p>
                    </div>
                  )}

                  <div>
                    <label className="block text-label-md text-neutral-700 mb-1.5">
                      Cover Letter (Optional)
                    </label>
                    <textarea
                      value={coverLetter}
                      onChange={(e) => setCoverLetter(e.target.value)}
                      placeholder="Tell the employer why you're a great fit for this role..."
                      rows={6}
                      className="block w-full rounded-lg border border-neutral-300 bg-white px-3.5 py-2 text-sm text-neutral-900 placeholder:text-neutral-400 focus:outline-none focus:ring-2 focus:ring-secondary-500/20 focus:border-secondary-500 resize-none"
                    />
                    <p className="mt-1 text-caption text-neutral-500">
                      A well-written cover letter can significantly increase your chances.
                    </p>
                  </div>

                  {applyError && (
                    <div className="flex items-center gap-2 rounded-lg bg-error-50 border border-error-200 p-3 text-body-sm text-error-600">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <circle cx="12" cy="12" r="10" />
                        <line x1="15" y1="9" x2="9" y2="15" />
                        <line x1="9" y1="9" x2="15" y2="15" />
                      </svg>
                      {applyError}
                    </div>
                  )}

                  <div className="flex gap-3 pt-2">
                    <Button
                      variant="outline"
                      fullWidth
                      onClick={() => setShowApplyModal(false)}
                      disabled={applying}
                    >
                      Cancel
                    </Button>
                    <Button
                      variant="primary"
                      fullWidth
                      onClick={handleApply}
                      loading={applying}
                    >
                      Submit Application
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      )}
    </div>
  );
}
