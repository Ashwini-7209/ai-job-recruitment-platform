import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Card, CardContent, Badge, Button, Skeleton } from '@/components/ui';
import { recruiterApplicationService, type ApplicationDetail, type ApplicationNote } from '@/services/recruiterApplication.service';
import { interviewService, type CreateInterviewData } from '@/services/interview.service';
import ScheduleInterviewModal from '@/components/recruiter/ScheduleInterviewModal';
import type { InterviewFormData } from '@/components/recruiter/ScheduleInterviewModal';
import { api } from '@/services/api';
import type { JobMatchResponse } from '@/services/ai.service';
import type { RecruiterInterviewDetail } from '@/types/recruiter';

const statusVariant: Record<string, 'info' | 'warning' | 'success' | 'error' | 'primary' | 'default'> = {
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

const validTransitions: Record<string, string[]> = {
  APPLIED: ['UNDER_REVIEW'],
  UNDER_REVIEW: ['SHORTLISTED', 'REJECTED'],
  SHORTLISTED: ['INTERVIEW', 'UNDER_REVIEW', 'REJECTED'],
  INTERVIEW: ['HIRED', 'REJECTED', 'SHORTLISTED'],
};

const transitionLabels: Record<string, string> = {
  UNDER_REVIEW: 'Move to Under Review',
  SHORTLISTED: 'Shortlist',
  INTERVIEW: 'Move to Interview',
  REJECTED: 'Reject',
  HIRED: 'Mark as Hired',
};

const transitionVariants: Record<string, 'primary' | 'danger' | 'secondary' | 'ghost'> = {
  UNDER_REVIEW: 'secondary',
  SHORTLISTED: 'primary',
  INTERVIEW: 'primary',
  REJECTED: 'danger',
  HIRED: 'primary',
};

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function formatDateTime(dateStr: string): string {
  return new Date(dateStr).toLocaleString('en-US', { month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

function formatFileSize(bytes: number | null): string {
  if (!bytes) return '';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function DetailSkeleton() {
  return (
    <div className="space-y-6">
      <Skeleton className="h-6 w-48" />
      <Card><CardContent className="p-5"><div className="space-y-4"><Skeleton className="h-5 w-40" /><Skeleton className="h-4 w-64" /><Skeleton className="h-4 w-48" /><Skeleton className="h-4 w-56" /></div></CardContent></Card>
      <Card><CardContent className="p-5"><div className="space-y-3"><Skeleton className="h-5 w-32" /><Skeleton className="h-20 w-full" /></div></CardContent></Card>
      <Card><CardContent className="p-5"><div className="space-y-3"><Skeleton className="h-5 w-32" /><Skeleton className="h-4 w-full" /><Skeleton className="h-4 w-3/4" /></div></CardContent></Card>
    </div>
  );
}

function CandidateInfoCard({ detail }: { detail: ApplicationDetail }) {
  const { candidate } = detail;
  const skills = candidate.parsedSkills ? candidate.parsedSkills.split(',').map((s) => s.trim()).filter(Boolean) : [];

  return (
    <Card>
      <CardContent className="p-5">
        <h3 className="text-heading-sm text-neutral-900 mb-4">Candidate Information</h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <p className="text-body-sm text-neutral-500">Full Name</p>
            <p className="text-body-md font-medium text-neutral-900">{candidate.fullName}</p>
          </div>
          <div>
            <p className="text-body-sm text-neutral-500">Email</p>
            <a href={`mailto:${candidate.email}`} className="text-body-md font-medium text-secondary-600 hover:text-secondary-700">{candidate.email}</a>
          </div>
          {candidate.headline && (
            <div>
              <p className="text-body-sm text-neutral-500">Headline</p>
              <p className="text-body-md text-neutral-900">{candidate.headline}</p>
            </div>
          )}
          {candidate.location && (
            <div>
              <p className="text-body-sm text-neutral-500">Location</p>
              <p className="text-body-md text-neutral-900">{candidate.location}</p>
            </div>
          )}
          {candidate.currentJobTitle && (
            <div>
              <p className="text-body-sm text-neutral-500">Current Position</p>
              <p className="text-body-md text-neutral-900">{candidate.currentJobTitle}</p>
            </div>
          )}
          {candidate.parsedYearsOfExperience != null && (
            <div>
              <p className="text-body-sm text-neutral-500">Years of Experience</p>
              <p className="text-body-md text-neutral-900">{candidate.parsedYearsOfExperience} years</p>
            </div>
          )}
        </div>

        {skills.length > 0 && (
          <div className="mt-4">
            <p className="text-body-sm text-neutral-500 mb-2">Skills</p>
            <div className="flex flex-wrap gap-1.5">
              {skills.map((skill) => (
                <span key={skill} className="text-xs bg-neutral-100 text-neutral-600 px-2 py-1 rounded-md">{skill}</span>
              ))}
            </div>
          </div>
        )}

        {candidate.educationSummary && (
          <div className="mt-4">
            <p className="text-body-sm text-neutral-500">Education</p>
            <p className="text-body-md text-neutral-900">{candidate.educationSummary}</p>
          </div>
        )}

        {(candidate.linkedinUrl || candidate.githubUrl || candidate.portfolioUrl) && (
          <div className="mt-4 pt-4 border-t border-neutral-100">
            <p className="text-body-sm text-neutral-500 mb-2">Links</p>
            <div className="flex flex-wrap gap-3">
              {candidate.linkedinUrl && (
                <a href={candidate.linkedinUrl} target="_blank" rel="noopener noreferrer" className="text-body-sm text-secondary-600 hover:text-secondary-700">LinkedIn</a>
              )}
              {candidate.githubUrl && (
                <a href={candidate.githubUrl} target="_blank" rel="noopener noreferrer" className="text-body-sm text-secondary-600 hover:text-secondary-700">GitHub</a>
              )}
              {candidate.portfolioUrl && (
                <a href={candidate.portfolioUrl} target="_blank" rel="noopener noreferrer" className="text-body-sm text-secondary-600 hover:text-secondary-700">Portfolio</a>
              )}
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function getScoreColor(score: number): string {
  if (score >= 80) return 'text-emerald-600';
  if (score >= 60) return 'text-amber-600';
  return 'text-red-600';
}

function getScoreBg(score: number): string {
  if (score >= 80) return 'bg-emerald-50 border-emerald-200';
  if (score >= 60) return 'bg-amber-50 border-amber-200';
  return 'bg-red-50 border-red-200';
}

function getScoreRing(score: number): string {
  if (score >= 80) return 'stroke-emerald-500';
  if (score >= 60) return 'stroke-amber-500';
  return 'stroke-red-500';
}

function ScoreCircle({ score, size = 64 }: { score: number; size?: number }) {
  const radius = (size - 8) / 2;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference - (score / 100) * circumference;

  return (
    <div className="relative" style={{ width: size, height: size }}>
      <svg width={size} height={size} className="-rotate-90">
        <circle cx={size / 2} cy={size / 2} r={radius} fill="none" stroke="currentColor" strokeWidth="4" className="text-neutral-100" />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          strokeWidth="4"
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          className={`${getScoreRing(score)} transition-all duration-700`}
        />
      </svg>
      <div className="absolute inset-0 flex items-center justify-center">
        <span className={`text-heading-sm font-bold ${getScoreColor(score)}`}>{score}%</span>
      </div>
    </div>
  );
}

function AICandidateMatchCard({ applicationId }: { applicationId: number }) {
  const [matchData, setMatchData] = useState<JobMatchResponse | null>(null);
  const [matchLoading, setMatchLoading] = useState(true);
  const [matchError, setMatchError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    const fetchMatch = async () => {
      try {
        setMatchLoading(true);
        setMatchError(null);
        const res = await api.get<JobMatchResponse>(`/recruiters/me/applications/${applicationId}/match`);
        if (!cancelled && res.success && res.data) {
          setMatchData(res.data);
        } else if (!cancelled) {
          setMatchError('Match data not available.');
        }
      } catch {
        if (!cancelled) setMatchError('Unable to load AI match analysis.');
      } finally {
        if (!cancelled) setMatchLoading(false);
      }
    };
    fetchMatch();
    return () => { cancelled = true; };
  }, [applicationId]);

  if (matchLoading) {
    return (
      <Card>
        <CardContent className="p-5">
          <Skeleton className="h-5 w-48 mb-4" />
          <div className="flex items-center gap-4">
            <Skeleton className="h-16 w-16 rounded-full" />
            <div className="space-y-2 flex-1">
              <Skeleton className="h-4 w-32" />
              <Skeleton className="h-4 w-48" />
            </div>
          </div>
        </CardContent>
      </Card>
    );
  }

  if (matchError || !matchData) {
    return (
      <Card>
        <CardContent className="p-5">
          <h3 className="text-heading-sm text-neutral-900 mb-3">AI Candidate Match</h3>
          <p className="text-body-sm text-neutral-500">{matchError || 'No match data available.'}</p>
        </CardContent>
      </Card>
    );
  }

  const overallScore = Math.round(matchData.overallScore);
  const skillScore = Math.round(matchData.skillScore);
  const experienceScore = Math.round(matchData.experienceScore);
  const profileScore = Math.round(matchData.profileScore);

  return (
    <Card>
      <CardContent className="p-5">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-heading-sm text-neutral-900">AI Candidate Match</h3>
          {matchData.aiUsed ? (
            <span className="inline-flex items-center gap-1 text-xs font-medium text-secondary-700 bg-secondary-50 border border-secondary-200 px-2 py-0.5 rounded-full">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M12 2l1.09 3.26L16 6l-2.91.74L12 10l-1.09-3.26L8 6l2.91-.74L12 2z" />
                <path d="M18 14l.73 2.19L21 17l-2.27.81L18 20l-.73-2.19L15 17l2.27-.81L18 14z" />
                <path d="M6 14l.73 2.19L9 17l-2.27.81L6 20l-.73-2.19L3 17l2.27-.81L6 14z" />
              </svg>
              AI-enhanced
            </span>
          ) : (
            <span className="text-xs text-neutral-400 bg-neutral-50 border border-neutral-200 px-2 py-0.5 rounded-full">Calculated</span>
          )}
        </div>

        <div className="flex items-start gap-5 mb-5">
          <div className="flex flex-col items-center">
            <ScoreCircle score={overallScore} size={72} />
            <span className="text-body-sm text-neutral-500 mt-1">Overall</span>
          </div>
          <div className="flex-1 space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-body-sm text-neutral-600">Skills</span>
              <div className="flex items-center gap-2">
                <div className="w-24 h-2 rounded-full bg-neutral-100 overflow-hidden">
                  <div className={`h-full rounded-full transition-all duration-700 ${getScoreBg(skillScore).split(' ')[0]}`} style={{ width: `${skillScore}%` }} />
                </div>
                <span className={`text-body-sm font-medium ${getScoreColor(skillScore)}`}>{skillScore}%</span>
              </div>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-body-sm text-neutral-600">Experience</span>
              <div className="flex items-center gap-2">
                <div className="w-24 h-2 rounded-full bg-neutral-100 overflow-hidden">
                  <div className={`h-full rounded-full transition-all duration-700 ${getScoreBg(experienceScore).split(' ')[0]}`} style={{ width: `${experienceScore}%` }} />
                </div>
                <span className={`text-body-sm font-medium ${getScoreColor(experienceScore)}`}>{experienceScore}%</span>
              </div>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-body-sm text-neutral-600">Profile</span>
              <div className="flex items-center gap-2">
                <div className="w-24 h-2 rounded-full bg-neutral-100 overflow-hidden">
                  <div className={`h-full rounded-full transition-all duration-700 ${getScoreBg(profileScore).split(' ')[0]}`} style={{ width: `${profileScore}%` }} />
                </div>
                <span className={`text-body-sm font-medium ${getScoreColor(profileScore)}`}>{profileScore}%</span>
              </div>
            </div>
            {matchData.semanticScore != null && (
              <div className="flex items-center justify-between">
                <span className="text-body-sm text-neutral-600">Semantic</span>
                <div className="flex items-center gap-2">
                  <div className="w-24 h-2 rounded-full bg-neutral-100 overflow-hidden">
                    <div className={`h-full rounded-full transition-all duration-700 ${getScoreBg(Math.round(matchData.semanticScore)).split(' ')[0]}`} style={{ width: `${Math.round(matchData.semanticScore)}%` }} />
                  </div>
                  <span className={`text-body-sm font-medium ${getScoreColor(Math.round(matchData.semanticScore))}`}>{Math.round(matchData.semanticScore)}%</span>
                </div>
              </div>
            )}
          </div>
        </div>

        {matchData.matchedSkills.length > 0 && (
          <div className="mb-4">
            <p className="text-body-sm text-neutral-500 mb-2">Matching Skills</p>
            <div className="flex flex-wrap gap-1.5">
              {matchData.matchedSkills.map((skill) => (
                <span key={skill} className="text-xs bg-emerald-50 text-emerald-700 border border-emerald-200 px-2 py-1 rounded-md">{skill}</span>
              ))}
            </div>
          </div>
        )}

        {matchData.missingSkills.length > 0 && (
          <div className="mb-4">
            <p className="text-body-sm text-neutral-500 mb-2">Missing Skills</p>
            <div className="flex flex-wrap gap-1.5">
              {matchData.missingSkills.map((skill) => (
                <span key={skill} className="text-xs bg-red-50 text-red-600 border border-red-200 px-2 py-1 rounded-md">{skill}</span>
              ))}
            </div>
          </div>
        )}

        {matchData.matchingReasons.length > 0 && (
          <div className="mb-4">
            <p className="text-body-sm text-neutral-500 mb-2">Why this match</p>
            <ul className="space-y-1.5">
              {matchData.matchingReasons.map((reason, i) => (
                <li key={i} className="flex items-start gap-2 text-body-sm text-neutral-700">
                  <span className="mt-1.5 h-1.5 w-1.5 rounded-full bg-emerald-500 shrink-0" />
                  {reason}
                </li>
              ))}
            </ul>
          </div>
        )}

        {matchData.potentialGaps.length > 0 && (
          <div className="mb-4">
            <p className="text-body-sm text-neutral-500 mb-2">Potential Gaps</p>
            <ul className="space-y-1.5">
              {matchData.potentialGaps.map((gap, i) => (
                <li key={i} className="flex items-start gap-2 text-body-sm text-neutral-700">
                  <span className="mt-1.5 h-1.5 w-1.5 rounded-full bg-amber-500 shrink-0" />
                  {gap}
                </li>
              ))}
            </ul>
          </div>
        )}

        {matchData.aiExplanation && (
          <div className="mt-4 pt-4 border-t border-neutral-100">
            <p className="text-body-sm text-neutral-500 mb-1">AI Analysis</p>
            <p className="text-body-sm text-neutral-700 leading-relaxed">{matchData.aiExplanation}</p>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function StatusManager({ detail, onStatusUpdate, onScheduleInterview }: { detail: ApplicationDetail; onStatusUpdate: () => void; onScheduleInterview: () => void }) {
  const [updating, setUpdating] = useState(false);
  const [showUnhireModal, setShowUnhireModal] = useState(false);
  const [unhireReason, setUnhireReason] = useState('');
  const [unhiring, setUnhiring] = useState(false);
  const [unhireError, setUnhireError] = useState<string | null>(null);
  const transitions = validTransitions[detail.status] || [];
  const isHired = detail.status === 'HIRED';

  const handleUpdate = async (newStatus: string) => {
    try {
      setUpdating(true);
      await recruiterApplicationService.updateStatus(detail.applicationId, newStatus);
      onStatusUpdate();
    } catch {
      alert('Failed to update status.');
    } finally {
      setUpdating(false);
    }
  };

  const handleUnhire = async () => {
    if (!unhireReason.trim()) {
      setUnhireError('Please provide a reason for this action.');
      return;
    }
    try {
      setUnhiring(true);
      setUnhireError(null);
      await recruiterApplicationService.unhireApplication(detail.applicationId, unhireReason.trim());
      setShowUnhireModal(false);
      setUnhireReason('');
      onStatusUpdate();
    } catch {
      setUnhireError('Failed to reopen application. Please try again.');
    } finally {
      setUnhiring(false);
    }
  };

  const openUnhireModal = () => {
    setUnhireReason('');
    setUnhireError(null);
    setShowUnhireModal(true);
  };

  return (
    <>
      <Card>
        <CardContent className="p-5">
          <h3 className="text-heading-sm text-neutral-900 mb-4">Status</h3>
          <div className="flex items-center gap-3 mb-4">
            <Badge variant={statusVariant[detail.status] || 'default'} size="lg" dot>
              {statusLabels[detail.status] || detail.status}
            </Badge>
            <span className="text-body-sm text-neutral-500">Last updated {formatDate(detail.updatedAt)}</span>
          </div>

          {transitions.length > 0 && (
            <div className="flex flex-wrap gap-2">
              {transitions.map((s) => (
                <Button
                  key={s}
                  variant={transitionVariants[s] || 'outline'}
                  size="sm"
                  disabled={updating}
                  onClick={() => handleUpdate(s)}
                >
                  {transitionLabels[s] || s}
                </Button>
              ))}
              <Button
                variant="primary"
                size="sm"
                disabled={updating}
                onClick={onScheduleInterview}
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="mr-1">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
                  <line x1="16" y1="2" x2="16" y2="6" />
                  <line x1="8" y1="2" x2="8" y2="6" />
                  <line x1="3" y1="10" x2="21" y2="10" />
                </svg>
                Schedule Interview
              </Button>
            </div>
          )}

          {!transitions.length && (
            <div className="flex flex-wrap gap-2">
              <Button
                variant="primary"
                size="sm"
                disabled={updating}
                onClick={onScheduleInterview}
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="mr-1">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
                  <line x1="16" y1="2" x2="16" y2="6" />
                  <line x1="8" y1="2" x2="8" y2="6" />
                  <line x1="3" y1="10" x2="21" y2="10" />
                </svg>
                Schedule Interview
              </Button>
            </div>
          )}

          {isHired && (
            <div className="mt-3">
              <Button variant="danger" size="sm" onClick={openUnhireModal}>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="mr-1">
                  <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" />
                  <path d="M3 3v5h5" />
                </svg>
                Unhire / Reopen
              </Button>
            </div>
          )}
        </CardContent>
      </Card>

      {showUnhireModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-neutral-900/50 backdrop-blur-sm">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full mx-4 p-6 space-y-4">
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-full bg-red-50">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-red-600">
                  <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
                  <line x1="12" y1="9" x2="12" y2="13" />
                  <line x1="12" y1="17" x2="12.01" y2="17" />
                </svg>
              </div>
              <div>
                <h3 className="text-heading-sm text-neutral-900">Unhire / Reopen Application</h3>
                <p className="text-body-sm text-neutral-500">This action will change the application status</p>
              </div>
            </div>

            <div className="bg-amber-50 border border-amber-200 rounded-lg p-3">
              <p className="text-body-sm text-amber-800">
                The candidate will no longer be marked as <strong>Hired</strong> for this position. The application will be moved back to <strong>Under Review</strong> status.
              </p>
            </div>

            <div>
              <label className="text-body-sm font-medium text-neutral-700 block mb-1.5">
                Reason for this change <span className="text-red-500">*</span>
              </label>
              <textarea
                className="w-full rounded-lg border border-neutral-200 px-3 py-2 text-body-sm focus:outline-none focus:ring-2 focus:ring-secondary-500 min-h-[80px] resize-y"
                placeholder="e.g. Position no longer available, offer declined, error in hiring decision..."
                value={unhireReason}
                onChange={(e) => {
                  setUnhireReason(e.target.value);
                  setUnhireError(null);
                }}
                disabled={unhiring}
              />
              {unhireError && (
                <p className="text-body-sm text-red-600 mt-1">{unhireError}</p>
              )}
            </div>

            <div className="flex items-center gap-3 justify-end pt-2">
              <Button
                variant="ghost"
                onClick={() => setShowUnhireModal(false)}
                disabled={unhiring}
              >
                Cancel
              </Button>
              <Button
                variant="danger"
                onClick={handleUnhire}
                disabled={unhiring || !unhireReason.trim()}
                loading={unhiring}
              >
                {unhiring ? 'Processing...' : 'Confirm Unhire'}
              </Button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

function StatusTimeline({ history }: { history: ApplicationDetail['statusHistory'] }) {
  if (!history || history.length === 0) return null;

  return (
    <Card>
      <CardContent className="p-5">
        <h3 className="text-heading-sm text-neutral-900 mb-4">Status History</h3>
        <div className="relative">
          <div className="absolute left-3 top-2 bottom-2 w-px bg-neutral-200" />
          <div className="space-y-4">
            {history.map((entry) => (
              <div key={entry.id} className="flex gap-4 relative">
                <div className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-neutral-100 z-10">
                  <div className="h-2 w-2 rounded-full bg-neutral-400" />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 flex-wrap">
                    {entry.oldStatus && (
                      <Badge variant={statusVariant[entry.oldStatus] || 'default'} size="sm">
                        {statusLabels[entry.oldStatus] || entry.oldStatus}
                      </Badge>
                    )}
                    {entry.oldStatus && (
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="text-neutral-400 shrink-0" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3" />
                      </svg>
                    )}
                    <Badge variant={statusVariant[entry.newStatus] || 'default'} size="sm">
                      {statusLabels[entry.newStatus] || entry.newStatus}
                    </Badge>
                  </div>
                  <p className="text-body-sm text-neutral-500 mt-1">
                    by {entry.changedByName} · {formatDateTime(entry.changedAt)}
                  </p>
                  {entry.reason && (
                    <p className="text-body-sm text-neutral-600 mt-1 italic">
                      Reason: {entry.reason}
                    </p>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

function InterviewInfoCard({ applicationId }: { applicationId: number }) {
  const [interviews, setInterviews] = useState<RecruiterInterviewDetail[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    const fetchInterviews = async () => {
      try {
        setLoading(true);
        const list = await interviewService.getApplicationInterviews(applicationId);
        if (!cancelled) setInterviews(list);
      } catch {
        if (!cancelled) setInterviews([]);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    fetchInterviews();
    return () => { cancelled = true; };
  }, [applicationId]);

  if (loading) {
    return (
      <Card>
        <CardContent className="p-5">
          <Skeleton className="h-5 w-48 mb-3" />
          <Skeleton className="h-4 w-64" />
        </CardContent>
      </Card>
    );
  }

  if (interviews.length === 0) return null;

  const interviewTypeLabels: Record<string, string> = { VIDEO: 'Video Call', PHONE: 'Phone', IN_PERSON: 'In Person' };
  const interviewStatusVariant: Record<string, 'success' | 'warning' | 'error' | 'info' | 'default'> = {
    SCHEDULED: 'info', RESCHEDULED: 'warning', COMPLETED: 'success', CANCELLED: 'default',
  };

  return (
    <Card>
      <CardContent className="p-5">
        <h3 className="text-heading-sm text-neutral-900 mb-4">Scheduled Interviews</h3>
        <div className="space-y-3">
          {interviews.map((iv) => (
            <div key={iv.interviewId} className="rounded-lg border border-neutral-100 p-3">
              <div className="flex items-start justify-between gap-2">
                <div>
                  <p className="text-body-sm font-medium text-neutral-900">{iv.title}</p>
                  <p className="text-body-sm text-neutral-500 mt-0.5">
                    {interviewTypeLabels[iv.interviewType] || iv.interviewType}
                    {iv.interviewerName && ` · ${iv.interviewerName}`}
                  </p>
                </div>
                <Badge variant={interviewStatusVariant[iv.status] || 'default'} size="sm" dot>{iv.status}</Badge>
              </div>
              <div className="mt-2 text-body-sm text-neutral-500">
                <p>{formatDateTime(iv.scheduledStart)} — {new Date(iv.scheduledEnd).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}</p>
                {iv.meetingLink && <p className="text-secondary-600 break-all">{iv.meetingLink}</p>}
                {iv.location && <p>{iv.location}</p>}
              </div>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}

function NotesSection({ detail, onNoteAdded }: { detail: ApplicationDetail; onNoteAdded: () => void }) {
  const [notes, setNotes] = useState<ApplicationNote[]>(detail.notes || []);
  const [newNote, setNewNote] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    setNotes(detail.notes || []);
  }, [detail.notes]);

  const handleAddNote = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newNote.trim()) return;
    try {
      setSubmitting(true);
      const note = await recruiterApplicationService.addNote(detail.applicationId, newNote.trim());
      setNotes((prev) => [note, ...prev]);
      setNewNote('');
      onNoteAdded();
    } catch {
      alert('Failed to add note.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Card>
      <CardContent className="p-5">
        <h3 className="text-heading-sm text-neutral-900 mb-4">Notes</h3>

        <form onSubmit={handleAddNote} className="mb-4">
          <textarea
            className="w-full rounded-lg border border-neutral-200 px-3 py-2 text-body-sm focus:outline-none focus:ring-2 focus:ring-secondary-500 min-h-[80px] resize-y"
            placeholder="Add a note about this candidate..."
            value={newNote}
            onChange={(e) => setNewNote(e.target.value)}
          />
          <div className="flex justify-end mt-2">
            <Button type="submit" size="sm" disabled={!newNote.trim() || submitting}>
              {submitting ? 'Adding...' : 'Add Note'}
            </Button>
          </div>
        </form>

        {notes.length === 0 ? (
          <p className="text-body-sm text-neutral-400 text-center py-4">No notes yet</p>
        ) : (
          <div className="space-y-3">
            {notes.map((note) => (
              <div key={note.id} className="rounded-lg border border-neutral-100 p-3">
                <p className="text-body-sm text-neutral-700 whitespace-pre-wrap">{note.note}</p>
                <div className="flex items-center gap-2 mt-2 text-body-sm text-neutral-400">
                  <span>{note.recruiterName}</span>
                  <span>·</span>
                  <span>{formatDateTime(note.createdAt)}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}

export default function RecruiterApplicationDetailPage() {
  const { applicationId } = useParams<{ applicationId: string }>();
  const navigate = useNavigate();
  const [detail, setDetail] = useState<ApplicationDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showScheduleModal, setShowScheduleModal] = useState(false);

  const fetchDetail = useCallback(async () => {
    if (!applicationId) return;
    try {
      setLoading(true);
      setError(null);
      const data = await recruiterApplicationService.getApplicationDetail(Number(applicationId));
      setDetail(data);
    } catch {
      setError('Failed to load application details.');
    } finally {
      setLoading(false);
    }
  }, [applicationId]);

  useEffect(() => {
    fetchDetail();
  }, [fetchDetail]);

  const handleScheduleInterview = async (formData: InterviewFormData) => {
    if (!applicationId) return;
    const payload: CreateInterviewData = {
      title: formData.title,
      interviewType: formData.interviewType,
      scheduledStart: formData.scheduledStart,
      scheduledEnd: formData.scheduledEnd,
      location: formData.location || undefined,
      meetingLink: formData.meetingLink || undefined,
      interviewerName: formData.interviewerName || undefined,
      interviewerNotes: formData.interviewerNotes || undefined,
      candidateNotes: formData.candidateNotes || undefined,
    };
    await interviewService.createInterview(Number(applicationId), payload);
    setShowScheduleModal(false);
    await fetchDetail();
  };

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <Button variant="ghost" size="sm" onClick={() => navigate('/recruiter/applications')}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" className="mr-1">
            <path d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" />
          </svg>
          Back to Applications
        </Button>
      </div>

      {loading ? (
        <DetailSkeleton />
      ) : error ? (
        <Card>
          <CardContent className="p-8 text-center">
            <p className="text-body-md text-error-600 mb-4">{error}</p>
            <Button variant="outline" onClick={fetchDetail}>Try Again</Button>
          </CardContent>
        </Card>
      ) : !detail ? null : (
        <>
          <div>
            <h2 className="text-heading-lg text-neutral-900">{detail.candidate.fullName}</h2>
            <p className="text-body-md text-neutral-500 mt-1">Application for {detail.candidate.email}</p>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2 space-y-6">
              <CandidateInfoCard detail={detail} />

              {detail.applicationId && <AICandidateMatchCard applicationId={detail.applicationId} />}

              {detail.coverLetter && (
                <Card>
                  <CardContent className="p-5">
                    <h3 className="text-heading-sm text-neutral-900 mb-3">Cover Letter</h3>
                    <p className="text-body-sm text-neutral-700 whitespace-pre-wrap leading-relaxed">{detail.coverLetter}</p>
                  </CardContent>
                </Card>
              )}

              {detail.resumeFileName && (
                <Card>
                  <CardContent className="p-5">
                    <h3 className="text-heading-sm text-neutral-900 mb-3">Resume</h3>
                    <div className="flex items-center gap-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-neutral-100">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="text-neutral-500" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m2.25 0H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
                        </svg>
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-body-sm font-medium text-neutral-900 truncate">{detail.resumeFileName}</p>
                        <p className="text-body-sm text-neutral-500">{formatFileSize(detail.resumeFileSize)}</p>
                      </div>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => {
                          recruiterApplicationService.downloadResume(detail.applicationId, detail.resumeFileName || 'resume');
                        }}
                      >
                        Download
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              )}
            </div>

            <div className="space-y-6">
              <StatusManager detail={detail} onStatusUpdate={fetchDetail} onScheduleInterview={() => setShowScheduleModal(true)} />
              <InterviewInfoCard applicationId={detail.applicationId} />
              <StatusTimeline history={detail.statusHistory} />
              <NotesSection detail={detail} onNoteAdded={fetchDetail} />
            </div>
          </div>
        </>
      )}

      {showScheduleModal && detail && (
        <ScheduleInterviewModal
          applicationId={detail.applicationId}
          candidateName={detail.candidate.fullName}
          jobTitle={detail.jobTitle}
          onSubmit={handleScheduleInterview}
          onClose={() => setShowScheduleModal(false)}
        />
      )}
    </div>
  );
}
