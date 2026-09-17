import { useState } from 'react';
import { Card, Button, Input, Select } from '@/components/ui';

interface ScheduleInterviewModalProps {
  applicationId: number;
  candidateName: string;
  jobTitle: string;
  onSubmit: (data: InterviewFormData) => Promise<void>;
  onClose: () => void;
}

export interface InterviewFormData {
  title: string;
  interviewType: 'VIDEO' | 'PHONE' | 'IN_PERSON';
  scheduledStart: string;
  scheduledEnd: string;
  location: string;
  meetingLink: string;
  interviewerName: string;
  interviewerNotes: string;
  candidateNotes: string;
}

interface FormErrors {
  title?: string;
  interviewType?: string;
  scheduledStart?: string;
  scheduledEnd?: string;
  location?: string;
  meetingLink?: string;
}

const interviewTypeOptions = [
  { value: 'VIDEO', label: 'Video Call' },
  { value: 'PHONE', label: 'Phone' },
  { value: 'IN_PERSON', label: 'In Person' },
];

function toLocalDatetimeString(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  const h = String(date.getHours()).padStart(2, '0');
  const min = String(date.getMinutes()).padStart(2, '0');
  return `${y}-${m}-${d}T${h}:${min}`;
}

function getDefaultStart(): string {
  const now = new Date();
  now.setMinutes(now.getMinutes() + 30);
  now.setSeconds(0, 0);
  return toLocalDatetimeString(now);
}

function getDefaultEnd(): string {
  const now = new Date();
  now.setMinutes(now.getMinutes() + 90);
  now.setSeconds(0, 0);
  return toLocalDatetimeString(now);
}

function validate(data: InterviewFormData): FormErrors {
  const errors: FormErrors = {};

  if (!data.title.trim()) {
    errors.title = 'Interview title is required';
  }

  if (!data.interviewType) {
    errors.interviewType = 'Interview type is required';
  }

  if (!data.scheduledStart) {
    errors.scheduledStart = 'Start time is required';
  } else {
    const startDate = new Date(data.scheduledStart);
    if (startDate <= new Date()) {
      errors.scheduledStart = 'Interview must be scheduled for a future time';
    }
  }

  if (!data.scheduledEnd) {
    errors.scheduledEnd = 'End time is required';
  } else if (data.scheduledStart && data.scheduledEnd) {
    const start = new Date(data.scheduledStart);
    const end = new Date(data.scheduledEnd);
    if (end <= start) {
      errors.scheduledEnd = 'End time must be after start time';
    }
  }

  if (data.interviewType === 'IN_PERSON' && !data.location.trim()) {
    errors.location = 'Location is required for in-person interviews';
  }

  if (data.interviewType === 'VIDEO' && !data.meetingLink.trim()) {
    errors.meetingLink = 'Meeting URL is required for video calls';
  }

  return errors;
}

export default function ScheduleInterviewModal({
  applicationId: _applicationId,
  candidateName,
  jobTitle,
  onSubmit,
  onClose,
}: ScheduleInterviewModalProps) {
  const [form, setForm] = useState<InterviewFormData>({
    title: `Interview - ${candidateName}`,
    interviewType: 'VIDEO',
    scheduledStart: getDefaultStart(),
    scheduledEnd: getDefaultEnd(),
    location: '',
    meetingLink: '',
    interviewerName: '',
    interviewerNotes: '',
    candidateNotes: '',
  });
  const [errors, setErrors] = useState<FormErrors>({});
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const handleChange = (field: keyof InterviewFormData, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    if (errors[field as keyof FormErrors]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
    setSubmitError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const validationErrors = validate(form);
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    try {
      setSubmitting(true);
      setSubmitError(null);
      await onSubmit({
        ...form,
        title: form.title.trim(),
        location: form.location.trim(),
        meetingLink: form.meetingLink.trim(),
        interviewerName: form.interviewerName.trim(),
        interviewerNotes: form.interviewerNotes.trim(),
        candidateNotes: form.candidateNotes.trim(),
      });
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } };
      setSubmitError(axiosError.response?.data?.message || 'Failed to schedule interview. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-neutral-900/50 backdrop-blur-sm">
      <div className="w-full max-w-lg mx-4 max-h-[90vh] overflow-y-auto">
        <Card padding="none" className="overflow-hidden">
          <div className="p-5 border-b border-neutral-100">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-heading-md text-neutral-900">Schedule Interview</h2>
                <p className="text-body-sm text-neutral-500 mt-1">
                  {candidateName} · {jobTitle}
                </p>
              </div>
              <button
                type="button"
                onClick={onClose}
                disabled={submitting}
                className="text-neutral-400 hover:text-neutral-600 transition-colors p-1"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <line x1="18" y1="6" x2="6" y2="18" />
                  <line x1="6" y1="6" x2="18" y2="18" />
                </svg>
              </button>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="p-5 space-y-4">
            {submitError && (
              <div className="rounded-lg bg-error-50 border border-error-200 p-3 text-body-sm text-error-700">
                {submitError}
              </div>
            )}

            <div>
              <label className="text-label-md text-neutral-700 block mb-1.5">Interview Title *</label>
              <Input
                placeholder="e.g. Technical Interview"
                value={form.title}
                onChange={(e) => handleChange('title', e.target.value)}
              />
              {errors.title && <p className="text-body-sm text-error-600 mt-1">{errors.title}</p>}
            </div>

            <div>
              <label className="text-label-md text-neutral-700 block mb-1.5">Interview Type *</label>
              <Select
                options={interviewTypeOptions}
                value={form.interviewType}
                onChange={(e) => handleChange('interviewType', e.target.value as 'VIDEO' | 'PHONE' | 'IN_PERSON')}
              />
              {errors.interviewType && <p className="text-body-sm text-error-600 mt-1">{errors.interviewType}</p>}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-label-md text-neutral-700 block mb-1.5">Start Date & Time *</label>
                <input
                  type="datetime-local"
                  className="w-full rounded-lg border border-neutral-200 px-3 py-2 text-body-sm focus:outline-none focus:ring-2 focus:ring-secondary-500 focus:border-secondary-500"
                  value={form.scheduledStart}
                  onChange={(e) => handleChange('scheduledStart', e.target.value)}
                  min={toLocalDatetimeString(new Date())}
                />
                {errors.scheduledStart && <p className="text-body-sm text-error-600 mt-1">{errors.scheduledStart}</p>}
              </div>
              <div>
                <label className="text-label-md text-neutral-700 block mb-1.5">End Date & Time *</label>
                <input
                  type="datetime-local"
                  className="w-full rounded-lg border border-neutral-200 px-3 py-2 text-body-sm focus:outline-none focus:ring-2 focus:ring-secondary-500 focus:border-secondary-500"
                  value={form.scheduledEnd}
                  onChange={(e) => handleChange('scheduledEnd', e.target.value)}
                  min={form.scheduledStart || toLocalDatetimeString(new Date())}
                />
                {errors.scheduledEnd && <p className="text-body-sm text-error-600 mt-1">{errors.scheduledEnd}</p>}
              </div>
            </div>

            {form.interviewType === 'VIDEO' && (
              <div>
                <label className="text-label-md text-neutral-700 block mb-1.5">Meeting URL *</label>
                <Input
                  placeholder="https://meet.google.com/... or https://zoom.us/..."
                  value={form.meetingLink}
                  onChange={(e) => handleChange('meetingLink', e.target.value)}
                />
                {errors.meetingLink && <p className="text-body-sm text-error-600 mt-1">{errors.meetingLink}</p>}
              </div>
            )}

            {form.interviewType === 'IN_PERSON' && (
              <div>
                <label className="text-label-md text-neutral-700 block mb-1.5">Location *</label>
                <Input
                  placeholder="e.g. Conference Room A, 123 Main St"
                  value={form.location}
                  onChange={(e) => handleChange('location', e.target.value)}
                />
                {errors.location && <p className="text-body-sm text-error-600 mt-1">{errors.location}</p>}
              </div>
            )}

            <div>
              <label className="text-label-md text-neutral-700 block mb-1.5">Interviewer Name</label>
              <Input
                placeholder="Who will conduct the interview?"
                value={form.interviewerName}
                onChange={(e) => handleChange('interviewerName', e.target.value)}
              />
            </div>

            <div>
              <label className="text-label-md text-neutral-700 block mb-1.5">Interviewer Notes</label>
              <textarea
                className="w-full rounded-lg border border-neutral-200 px-3 py-2 text-body-sm focus:outline-none focus:ring-2 focus:ring-secondary-500 min-h-[72px] resize-y"
                placeholder="Internal notes for the interviewer..."
                value={form.interviewerNotes}
                onChange={(e) => handleChange('interviewerNotes', e.target.value)}
              />
            </div>

            <div>
              <label className="text-label-md text-neutral-700 block mb-1.5">Candidate Notes</label>
              <textarea
                className="w-full rounded-lg border border-neutral-200 px-3 py-2 text-body-sm focus:outline-none focus:ring-2 focus:ring-secondary-500 min-h-[72px] resize-y"
                placeholder="Instructions or info for the candidate..."
                value={form.candidateNotes}
                onChange={(e) => handleChange('candidateNotes', e.target.value)}
              />
            </div>

            <div className="flex items-center gap-3 justify-end pt-2 border-t border-neutral-100">
              <Button variant="ghost" type="button" onClick={onClose} disabled={submitting}>
                Cancel
              </Button>
              <Button type="submit" loading={submitting}>
                Schedule Interview
              </Button>
            </div>
          </form>
        </Card>
      </div>
    </div>
  );
}
