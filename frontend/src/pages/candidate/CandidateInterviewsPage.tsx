import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, Badge, Button, Skeleton, EmptyState } from '@/components/ui';
import { interviewService } from '@/services/interview.service';
import type { CandidateInterviewDetail } from '@/types/candidate';

const typeLabels: Record<string, string> = {
  VIDEO: 'Video Call',
  PHONE: 'Phone',
  IN_PERSON: 'In Person',
};

const typeIcons: Record<string, string> = {
  VIDEO: 'M15.75 10.5l4.72-4.72a.75.75 0 011.28.53v11.38a.75.75 0 01-1.28.53l-4.72-4.72M4.5 18.75h9a2.25 2.25 0 002.25-2.25v-9a2.25 2.25 0 00-2.25-2.25h-9A2.25 2.25 0 002.25 7.5v9a2.25 2.25 0 002.25 2.25z',
  PHONE: 'M2.25 6.75c0 8.284 6.716 15 15 15h2.25a2.25 2.25 0 002.25-2.25v-1.372c0-.516-.351-.966-.852-1.091l-4.423-1.106c-.44-.11-.902.055-1.173.417l-.97 1.293c-.282.376-.769.542-1.21.38a12.035 12.035 0 01-7.143-7.143c-.162-.441.004-.928.38-1.21l1.293-.97c.363-.271.527-.734.417-1.173L6.963 3.102a1.125 1.125 0 00-1.091-.852H4.5A2.25 2.25 0 002.25 4.5v2.25z',
  IN_PERSON: 'M15 10.5a3 3 0 11-6 0 3 3 0 016 0z M19.5 10.5c0 7.142-7.5 11.25-7.5 11.25S4.5 17.642 4.5 10.5a7.5 7.5 0 1115 0z',
};

const statusVariant: Record<string, 'info' | 'warning' | 'success' | 'error' | 'default' | 'primary'> = {
  SCHEDULED: 'info',
  RESCHEDULED: 'warning',
  COMPLETED: 'success',
  CANCELLED: 'error',
};

const statusLabels: Record<string, string> = {
  SCHEDULED: 'Scheduled',
  RESCHEDULED: 'Rescheduled',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
};

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function formatTime(dateStr: string): string {
  return new Date(dateStr).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
}

export default function CandidateInterviewsPage() {
  const navigate = useNavigate();
  const [interviews, setInterviews] = useState<CandidateInterviewDetail[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [selectedInterview, setSelectedInterview] = useState<CandidateInterviewDetail | null>(null);

  const fetchInterviews = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await interviewService.getCandidateInterviews({ page: currentPage, size: 10, sort: 'newest' });
      setInterviews(data.content);
      setTotalPages(data.totalPages);
    } catch {
      setError('Failed to load interviews.');
    } finally {
      setLoading(false);
    }
  }, [currentPage]);

  useEffect(() => {
    fetchInterviews();
  }, [fetchInterviews]);

  const upcomingInterviews = interviews.filter((i) => i.status === 'SCHEDULED' || i.status === 'RESCHEDULED');
  const pastInterviews = interviews.filter((i) => i.status !== 'SCHEDULED' && i.status !== 'RESCHEDULED');

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
      <div>
        <h2 className="text-heading-lg text-neutral-900">My Interviews</h2>
        <p className="text-body-md text-neutral-500 mt-1">View your upcoming and past interviews</p>
      </div>

      {loading ? (
        <div className="space-y-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="rounded-lg border border-neutral-200 p-4">
              <div className="flex items-center gap-3">
                <Skeleton className="h-10 w-10 rounded-lg" />
                <div className="flex-1">
                  <Skeleton className="h-5 w-48 mb-2" />
                  <Skeleton className="h-4 w-64" />
                </div>
                <Skeleton className="h-6 w-20 rounded-full" />
              </div>
            </div>
          ))}
        </div>
      ) : error ? (
        <Card>
          <CardContent className="p-8 text-center">
            <p className="text-body-md text-error-600 mb-4">{error}</p>
            <Button variant="outline" onClick={fetchInterviews}>Try Again</Button>
          </CardContent>
        </Card>
      ) : interviews.length === 0 ? (
        <Card>
          <CardContent className="p-8">
            <EmptyState
              icon={
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
                  <line x1="16" y1="2" x2="16" y2="6" />
                  <line x1="8" y1="2" x2="8" y2="6" />
                  <line x1="3" y1="10" x2="21" y2="10" />
                </svg>
              }
              title="No interviews scheduled"
              description="You don't have any interviews yet. Keep applying to jobs and you'll be invited for interviews."
              action={<Button onClick={() => navigate('/candidate/applications')}>View Applications</Button>}
            />
          </CardContent>
        </Card>
      ) : (
        <>
          {upcomingInterviews.length > 0 && (
            <div>
              <h3 className="text-heading-sm text-neutral-900 mb-3">Upcoming Interviews</h3>
              <div className="space-y-3">
                {upcomingInterviews.map((interview) => (
                  <div
                    key={interview.interviewId}
                    className="rounded-lg border border-neutral-200 p-4 hover:border-primary-200 hover:shadow-sm transition-all cursor-pointer"
                    onClick={() => setSelectedInterview(interview)}
                  >
                    <div className="flex items-start gap-3">
                      <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-primary-50 text-primary-600">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                          <path d={typeIcons[interview.interviewType]} />
                        </svg>
                      </div>
                      <div className="min-w-0 flex-1">
                        <div className="flex items-start justify-between gap-2">
                          <div className="min-w-0">
                            <h4 className="text-body-md font-semibold text-neutral-900 truncate">{interview.title}</h4>
                            <p className="text-body-sm text-neutral-500">{interview.jobTitle}</p>
                          </div>
                          <Badge variant={statusVariant[interview.status]} size="sm">{statusLabels[interview.status]}</Badge>
                        </div>
                        <div className="flex flex-wrap items-center gap-x-3 gap-y-1 mt-2 text-body-sm text-neutral-500">
                          <span className="font-medium text-neutral-700">{formatDate(interview.scheduledStart)}</span>
                          <span>{formatTime(interview.scheduledStart)} - {formatTime(interview.scheduledEnd)}</span>
                          <span>{typeLabels[interview.interviewType]}</span>
                          {interview.interviewerName && <span>with {interview.interviewerName}</span>}
                        </div>
                        {interview.meetingLink && (
                          <a
                            href={interview.meetingLink}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="inline-flex items-center gap-1 mt-2 text-body-sm font-medium text-primary-600 hover:text-primary-700"
                            onClick={(e) => e.stopPropagation()}
                          >
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                              <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
                              <polyline points="15,3 21,3 21,9" />
                              <line x1="10" y1="14" x2="21" y2="3" />
                            </svg>
                            Join Meeting
                          </a>
                        )}
                        {interview.location && (
                          <p className="text-body-sm text-neutral-500 mt-1">Location: {interview.location}</p>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {pastInterviews.length > 0 && (
            <div>
              <h3 className="text-heading-sm text-neutral-900 mb-3">Past Interviews</h3>
              <div className="space-y-3">
                {pastInterviews.map((interview) => (
                  <div
                    key={interview.interviewId}
                    className="rounded-lg border border-neutral-200 p-4 opacity-75 cursor-pointer hover:opacity-100 transition-all"
                    onClick={() => setSelectedInterview(interview)}
                  >
                    <div className="flex items-start gap-3">
                      <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-neutral-100 text-neutral-400">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                          <path d={typeIcons[interview.interviewType]} />
                        </svg>
                      </div>
                      <div className="min-w-0 flex-1">
                        <div className="flex items-start justify-between gap-2">
                          <div className="min-w-0">
                            <h4 className="text-body-md font-semibold text-neutral-900 truncate">{interview.title}</h4>
                            <p className="text-body-sm text-neutral-500">{interview.jobTitle}</p>
                          </div>
                          <Badge variant={statusVariant[interview.status]} size="sm">{statusLabels[interview.status]}</Badge>
                        </div>
                        <div className="flex flex-wrap items-center gap-x-3 gap-y-1 mt-2 text-body-sm text-neutral-500">
                          <span>{formatDate(interview.scheduledStart)}</span>
                          <span>{formatTime(interview.scheduledStart)} - {formatTime(interview.scheduledEnd)}</span>
                          <span>{typeLabels[interview.interviewType]}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 pt-4">
              <Button variant="outline" size="sm" disabled={currentPage === 0} onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}>Previous</Button>
              <span className="text-body-sm text-neutral-600 px-3">Page {currentPage + 1} of {totalPages}</span>
              <Button variant="outline" size="sm" disabled={currentPage >= totalPages - 1} onClick={() => setCurrentPage((p) => Math.min(totalPages - 1, p + 1))}>Next</Button>
            </div>
          )}
        </>
      )}

      {selectedInterview && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4" onClick={() => setSelectedInterview(null)}>
          <div className="bg-white rounded-xl max-w-lg w-full max-h-[90vh] overflow-y-auto" onClick={(e) => e.stopPropagation()}>
            <div className="p-5 border-b border-neutral-100 flex items-center justify-between">
              <h3 className="text-heading-sm text-neutral-900">Interview Details</h3>
              <Badge variant={statusVariant[selectedInterview.status]}>{statusLabels[selectedInterview.status]}</Badge>
            </div>
            <div className="p-5 space-y-4">
              <div>
                <h4 className="text-body-md font-semibold text-neutral-900">{selectedInterview.title}</h4>
                <p className="text-body-sm text-neutral-500 mt-1">{selectedInterview.jobTitle}</p>
              </div>
              <div className="grid grid-cols-2 gap-3 text-body-sm">
                <div>
                  <span className="text-neutral-500">Type</span>
                  <p className="font-medium text-neutral-900">{typeLabels[selectedInterview.interviewType]}</p>
                </div>
                <div>
                  <span className="text-neutral-500">Interviewer</span>
                  <p className="font-medium text-neutral-900">{selectedInterview.interviewerName || 'Not specified'}</p>
                </div>
                <div>
                  <span className="text-neutral-500">Date & Time</span>
                  <p className="font-medium text-neutral-900">{formatDate(selectedInterview.scheduledStart)}</p>
                  <p className="text-neutral-600">{formatTime(selectedInterview.scheduledStart)} - {formatTime(selectedInterview.scheduledEnd)}</p>
                </div>
                {selectedInterview.location && (
                  <div>
                    <span className="text-neutral-500">Location</span>
                    <p className="font-medium text-neutral-900">{selectedInterview.location}</p>
                  </div>
                )}
              </div>
              {selectedInterview.meetingLink && (
                <a
                  href={selectedInterview.meetingLink}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-2 p-3 rounded-lg border border-primary-200 bg-primary-50 text-primary-700 hover:bg-primary-100 transition-colors"
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
                  </svg>
                  Join Video Meeting
                </a>
              )}
              <div className="flex justify-end pt-2 border-t border-neutral-100">
                <Button variant="ghost" onClick={() => setSelectedInterview(null)}>Close</Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
