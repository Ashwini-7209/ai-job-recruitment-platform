import { useState, useEffect, useCallback } from 'react';
import { Card, CardContent, Badge, Button, Input, Select, Skeleton, EmptyState } from '@/components/ui';
import { interviewService, type UpdateInterviewData } from '@/services/interview.service';
import type { RecruiterInterviewDetail } from '@/types/recruiter';

const typeLabels: Record<string, string> = {
  VIDEO: 'Video Call',
  PHONE: 'Phone',
  IN_PERSON: 'In Person',
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
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return '-';
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function formatTime(dateStr: string): string {
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return '-';
  return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
}

function InterviewDetailModal({ interview, onClose, onUpdate, onCancel }: {
  interview: RecruiterInterviewDetail;
  onClose: () => void;
  onUpdate: () => void;
  onCancel: () => void;
}) {
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState<UpdateInterviewData>({
    title: interview.title,
    interviewType: interview.interviewType,
    scheduledStart: interview.scheduledStart.slice(0, 16),
    scheduledEnd: interview.scheduledEnd.slice(0, 16),
    location: interview.location || '',
    meetingLink: interview.meetingLink || '',
    interviewerName: interview.interviewerName || '',
    interviewerNotes: interview.interviewerNotes || '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setLoading(true);
      setError('');
      await interviewService.updateInterview(interview.interviewId, {
        ...form,
        scheduledStart: form.scheduledStart ? new Date(form.scheduledStart).toISOString() : undefined,
        scheduledEnd: form.scheduledEnd ? new Date(form.scheduledEnd).toISOString() : undefined,
      });
      setEditing(false);
      onUpdate();
    } catch {
      setError('Failed to update interview.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async () => {
    if (!window.confirm('Are you sure you want to cancel this interview?')) return;
    try {
      await interviewService.cancelInterview(interview.interviewId);
      onCancel();
    } catch {
      alert('Failed to cancel interview.');
    }
  };

  const canCancel = interview.status === 'SCHEDULED' || interview.status === 'RESCHEDULED';

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-3 sm:p-4" onClick={onClose}>
      <div className="bg-white rounded-xl max-w-lg w-full max-h-[90vh] overflow-y-auto" onClick={(e) => e.stopPropagation()}>
        <div className="p-4 sm:p-5 border-b border-neutral-100 flex items-center justify-between gap-2">
          <h3 className="text-heading-sm text-neutral-900">{editing ? 'Edit Interview' : 'Interview Details'}</h3>
          <Badge variant={statusVariant[interview.status]} size="sm">{statusLabels[interview.status]}</Badge>
        </div>
        {editing ? (
          <form onSubmit={handleUpdate} className="p-4 sm:p-5 space-y-4">
            {error && <p className="text-body-sm text-error-600 bg-error-50 rounded-lg p-3">{error}</p>}
            <div>
              <label className="text-label-md text-neutral-700 block mb-1">Title</label>
              <Input value={form.title || ''} onChange={(e) => setForm({ ...form, title: e.target.value })} />
            </div>
            <div>
              <label className="text-label-md text-neutral-700 block mb-1">Type</label>
              <Select
                options={[
                  { value: 'VIDEO', label: 'Video Call' },
                  { value: 'PHONE', label: 'Phone' },
                  { value: 'IN_PERSON', label: 'In Person' },
                ]}
                value={form.interviewType || 'VIDEO'}
                onChange={(e) => setForm({ ...form, interviewType: e.target.value as UpdateInterviewData['interviewType'] })}
              />
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label className="text-label-md text-neutral-700 block mb-1">Start</label>
                <input type="datetime-local" className="w-full rounded-lg border border-neutral-200 px-3 py-2 text-body-sm focus:outline-none focus:ring-2 focus:ring-secondary-500" value={form.scheduledStart || ''} onChange={(e) => setForm({ ...form, scheduledStart: e.target.value })} />
              </div>
              <div>
                <label className="text-label-md text-neutral-700 block mb-1">End</label>
                <input type="datetime-local" className="w-full rounded-lg border border-neutral-200 px-3 py-2 text-body-sm focus:outline-none focus:ring-2 focus:ring-secondary-500" value={form.scheduledEnd || ''} onChange={(e) => setForm({ ...form, scheduledEnd: e.target.value })} />
              </div>
            </div>
            <div>
              <label className="text-label-md text-neutral-700 block mb-1">Meeting Link</label>
              <Input value={form.meetingLink || ''} onChange={(e) => setForm({ ...form, meetingLink: e.target.value })} />
            </div>
            <div>
              <label className="text-label-md text-neutral-700 block mb-1">Notes</label>
              <textarea className="w-full rounded-lg border border-neutral-200 px-3 py-2 text-body-sm focus:outline-none focus:ring-2 focus:ring-secondary-500 min-h-[80px]" value={form.interviewerNotes || ''} onChange={(e) => setForm({ ...form, interviewerNotes: e.target.value })} />
            </div>
            <div className="flex gap-3 justify-end pt-2">
              <Button type="button" variant="outline" onClick={() => setEditing(false)}>Cancel</Button>
              <Button type="submit" disabled={loading}>{loading ? 'Saving...' : 'Save Changes'}</Button>
            </div>
          </form>
        ) : (
          <div className="p-4 sm:p-5 space-y-4">
            <div>
              <h4 className="text-body-md font-semibold text-neutral-900">{interview.title}</h4>
              <p className="text-body-sm text-neutral-500 mt-1">{interview.jobTitle} &middot; {interview.candidateName}</p>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-body-sm">
              <div>
                <span className="text-neutral-500">Type</span>
                <p className="font-medium text-neutral-900">{typeLabels[interview.interviewType] || interview.interviewType}</p>
              </div>
              <div>
                <span className="text-neutral-500">Interviewer</span>
                <p className="font-medium text-neutral-900">{interview.interviewerName || 'Not set'}</p>
              </div>
              <div>
                <span className="text-neutral-500">Date & Time</span>
                <p className="font-medium text-neutral-900">{formatDate(interview.scheduledStart)}</p>
                <p className="text-neutral-600">{formatTime(interview.scheduledStart)} - {formatTime(interview.scheduledEnd)}</p>
              </div>
              <div>
                <span className="text-neutral-500">Location</span>
                <p className="font-medium text-neutral-900">{interview.location || 'Not set'}</p>
              </div>
            </div>
            {interview.meetingLink && (
              <div className="text-body-sm">
                <span className="text-neutral-500">Meeting Link</span>
                <a href={interview.meetingLink} target="_blank" rel="noopener noreferrer" className="block font-medium text-secondary-600 hover:text-secondary-700 truncate">{interview.meetingLink}</a>
              </div>
            )}
            {interview.interviewerNotes && (
              <div className="text-body-sm">
                <span className="text-neutral-500">Notes</span>
                <p className="text-neutral-700 whitespace-pre-wrap">{interview.interviewerNotes}</p>
              </div>
            )}
            <div className="flex gap-3 justify-end pt-2 border-t border-neutral-100">
              {canCancel && (
                <Button variant="danger" onClick={handleCancel}>Cancel Interview</Button>
              )}
              {canCancel && (
                <Button variant="outline" onClick={() => setEditing(true)}>Edit / Reschedule</Button>
              )}
              <Button variant="ghost" onClick={onClose}>Close</Button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default function RecruiterInterviewsPage() {
  const [interviews, setInterviews] = useState<RecruiterInterviewDetail[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [sortOrder, setSortOrder] = useState('newest');
  const [selectedInterview, setSelectedInterview] = useState<RecruiterInterviewDetail | null>(null);

  const fetchInterviews = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await interviewService.getRecruiterInterviews({ page: currentPage, size: 10, sort: sortOrder });
      setInterviews(data.content);
      setTotalElements(data.totalElements);
      setTotalPages(data.totalPages);
    } catch {
      setError('Failed to load interviews.');
    } finally {
      setLoading(false);
    }
  }, [currentPage, sortOrder]);

  useEffect(() => {
    fetchInterviews();
  }, [fetchInterviews]);

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-heading-lg text-neutral-900">Interviews</h2>
          <p className="text-body-md text-neutral-500 mt-1">Manage candidate interviews</p>
        </div>
      </div>

      <Card>
        <CardContent className="p-5">
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="flex-1" />
            <div className="w-full sm:w-40">
              <Select
                options={[
                  { value: 'newest', label: 'Newest First' },
                  { value: 'oldest', label: 'Oldest First' },
                ]}
                value={sortOrder}
                onChange={(e) => { setSortOrder(e.target.value); setCurrentPage(0); }}
              />
            </div>
          </div>
        </CardContent>
      </Card>

      {loading ? (
        <div className="space-y-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="rounded-lg border border-neutral-200 p-4">
              <div className="flex items-center justify-between">
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
              description="Schedule interviews from your application management page."
            />
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="text-body-sm text-neutral-500">{totalElements} interview{totalElements !== 1 ? 's' : ''}</div>
          <div className="space-y-3">
            {interviews.map((interview) => (
              <div
                key={interview.interviewId}
                className="rounded-lg border border-neutral-200 p-4 hover:border-secondary-200 hover:shadow-sm transition-all cursor-pointer"
                onClick={() => setSelectedInterview(interview)}
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0 flex-1">
                    <h3 className="text-body-md font-semibold text-neutral-900 truncate">{interview.title}</h3>
                    <p className="text-body-sm text-neutral-500 mt-0.5">{interview.candidateName} &middot; {interview.jobTitle}</p>
                    <div className="flex flex-wrap items-center gap-x-3 gap-y-1 mt-2 text-body-sm text-neutral-500">
                      <span>{formatDate(interview.scheduledStart)}</span>
                      <span>{formatTime(interview.scheduledStart)} - {formatTime(interview.scheduledEnd)}</span>
                      <span>{typeLabels[interview.interviewType] || interview.interviewType}</span>
                      {interview.location && <span>{interview.location}</span>}
                    </div>
                  </div>
                  <Badge variant={statusVariant[interview.status]} size="sm">{statusLabels[interview.status]}</Badge>
                </div>
              </div>
            ))}
          </div>
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
        <InterviewDetailModal
          interview={selectedInterview}
          onClose={() => setSelectedInterview(null)}
          onUpdate={() => { setSelectedInterview(null); fetchInterviews(); }}
          onCancel={() => { setSelectedInterview(null); fetchInterviews(); }}
        />
      )}
    </div>
  );
}
