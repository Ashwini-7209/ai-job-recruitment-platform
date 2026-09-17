import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, CardContent, Badge, Button, Input, Select, Skeleton } from '@/components/ui';
import { recruiterJobService, type RecruiterJob, type UpdateJobData } from '@/services/recruiterJob.service';

const statusVariant: Record<string, 'default' | 'success' | 'error'> = {
  DRAFT: 'default',
  PUBLISHED: 'success',
  CLOSED: 'error',
};

const statusLabels: Record<string, string> = {
  DRAFT: 'Draft',
  PUBLISHED: 'Published',
  CLOSED: 'Closed',
};

const employmentTypeLabels: Record<string, string> = {
  FULL_TIME: 'Full Time',
  PART_TIME: 'Part Time',
  CONTRACT: 'Contract',
  INTERNSHIP: 'Internship',
  FREELANCE: 'Freelance',
};

const workplaceTypeLabels: Record<string, string> = {
  ONSITE: 'On-site',
  REMOTE: 'Remote',
  HYBRID: 'Hybrid',
};

const employmentTypeOptions = [
  { value: 'FULL_TIME', label: 'Full Time' },
  { value: 'PART_TIME', label: 'Part Time' },
  { value: 'CONTRACT', label: 'Contract' },
  { value: 'INTERNSHIP', label: 'Internship' },
  { value: 'FREELANCE', label: 'Freelance' },
];

const workplaceTypeOptions = [
  { value: 'ONSITE', label: 'On-site' },
  { value: 'REMOTE', label: 'Remote' },
  { value: 'HYBRID', label: 'Hybrid' },
];

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function formatDateTime(dateStr: string): string {
  return new Date(dateStr).toLocaleString('en-US', { month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

interface EditFormErrors {
  title?: string;
  description?: string;
  employmentType?: string;
  workplaceType?: string;
  experienceMin?: string;
  experienceMax?: string;
  salaryMin?: string;
  salaryMax?: string;
}

function DetailField({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div>
      <span className="text-body-sm text-neutral-500">{label}</span>
      <div className="text-body-md font-medium text-neutral-900 mt-0.5">{value || '—'}</div>
    </div>
  );
}

export default function RecruiterJobDetailPage() {
  const { jobId } = useParams<{ jobId: string }>();
  const navigate = useNavigate();
  const [job, setJob] = useState<RecruiterJob | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editing, setEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [editErrors, setEditErrors] = useState<EditFormErrors>({});

  const [editForm, setEditForm] = useState<UpdateJobData>({});

  const fetchJob = useCallback(async () => {
    if (!jobId) return;
    try {
      setLoading(true);
      setError(null);
      const data = await recruiterJobService.getJobById(Number(jobId));
      setJob(data);
    } catch {
      setError('Failed to load job details.');
    } finally {
      setLoading(false);
    }
  }, [jobId]);

  useEffect(() => {
    fetchJob();
  }, [fetchJob]);

  const startEditing = () => {
    if (!job) return;
    setEditForm({
      title: job.title,
      description: job.description,
      location: job.location || '',
      employmentType: job.employmentType,
      workplaceType: job.workplaceType,
      experienceMin: job.experienceMin ?? undefined,
      experienceMax: job.experienceMax ?? undefined,
      salaryMin: job.salaryMin ?? undefined,
      salaryMax: job.salaryMax ?? undefined,
      skills: job.skills || '',
      applicationDeadline: job.applicationDeadline ? job.applicationDeadline.slice(0, 16) : '',
    });
    setEditErrors({});
    setEditing(true);
  };

  const cancelEditing = () => {
    setEditing(false);
    setEditErrors({});
  };

  const validateEdit = (): boolean => {
    const newErrors: EditFormErrors = {};

    if (!editForm.title?.trim()) {
      newErrors.title = 'Title is required.';
    } else if (editForm.title.length > 200) {
      newErrors.title = 'Title must be 200 characters or less.';
    }

    if (!editForm.description?.trim()) {
      newErrors.description = 'Description is required.';
    } else if (editForm.description.length > 10000) {
      newErrors.description = 'Description must be 10,000 characters or less.';
    }

    if (!editForm.employmentType) {
      newErrors.employmentType = 'Employment type is required.';
    }

    if (!editForm.workplaceType) {
      newErrors.workplaceType = 'Workplace type is required.';
    }

    if (editForm.experienceMin !== undefined && editForm.experienceMin < 0) {
      newErrors.experienceMin = 'Must be 0 or greater.';
    }

    if (editForm.experienceMax !== undefined && editForm.experienceMax < 0) {
      newErrors.experienceMax = 'Must be 0 or greater.';
    }

    if (
      editForm.experienceMin !== undefined &&
      editForm.experienceMax !== undefined &&
      editForm.experienceMin > editForm.experienceMax
    ) {
      newErrors.experienceMax = 'Must be greater than or equal to minimum.';
    }

    if (editForm.salaryMin !== undefined && editForm.salaryMin < 0) {
      newErrors.salaryMin = 'Must be 0 or greater.';
    }

    if (editForm.salaryMax !== undefined && editForm.salaryMax < 0) {
      newErrors.salaryMax = 'Must be 0 or greater.';
    }

    if (
      editForm.salaryMin !== undefined &&
      editForm.salaryMax !== undefined &&
      editForm.salaryMin > editForm.salaryMax
    ) {
      newErrors.salaryMax = 'Must be greater than or equal to minimum.';
    }

    setEditErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSaveEdit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!job || !validateEdit()) return;

    try {
      setSaving(true);
      const payload: UpdateJobData = {
        title: editForm.title?.trim(),
        description: editForm.description?.trim(),
        employmentType: editForm.employmentType,
        workplaceType: editForm.workplaceType,
      };

      if (editForm.location?.trim()) payload.location = editForm.location.trim();
      else payload.location = '';
      if (editForm.experienceMin !== undefined) payload.experienceMin = editForm.experienceMin;
      if (editForm.experienceMax !== undefined) payload.experienceMax = editForm.experienceMax;
      if (editForm.salaryMin !== undefined) payload.salaryMin = editForm.salaryMin;
      if (editForm.salaryMax !== undefined) payload.salaryMax = editForm.salaryMax;
      if (editForm.skills?.trim()) payload.skills = editForm.skills.trim();
      else payload.skills = '';
      if (editForm.applicationDeadline) payload.applicationDeadline = new Date(editForm.applicationDeadline).toISOString();
      else payload.applicationDeadline = undefined;

      await recruiterJobService.updateJob(job.id, payload);
      setEditing(false);
      fetchJob();
    } catch {
      alert('Failed to save changes.');
    } finally {
      setSaving(false);
    }
  };

  const handlePublish = async () => {
    if (!job) return;
    if (!window.confirm('Are you sure you want to publish this job?')) return;
    try {
      setActionLoading(true);
      await recruiterJobService.publishJob(job.id);
      fetchJob();
    } catch {
      alert('Failed to publish job.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleClose = async () => {
    if (!job) return;
    if (!window.confirm('Are you sure you want to close this job?')) return;
    try {
      setActionLoading(true);
      await recruiterJobService.closeJob(job.id);
      fetchJob();
    } catch {
      alert('Failed to close job.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!job) return;
    if (!window.confirm('Are you sure you want to delete this job? This action cannot be undone.')) return;
    try {
      setActionLoading(true);
      await recruiterJobService.deleteJob(job.id);
      navigate('/recruiter/jobs');
    } catch {
      alert('Failed to delete job.');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
        <Skeleton className="h-8 w-64" />
        <Card>
          <CardContent className="p-5 space-y-4">
            <Skeleton className="h-6 w-48" />
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-3/4" />
            <div className="grid grid-cols-2 gap-4">
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-4 w-full" />
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (error || !job) {
    return (
      <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
        <Card>
          <CardContent className="p-8 text-center">
            <p className="text-body-md text-error-600 mb-4">{error || 'Job not found.'}</p>
            <Button variant="outline" onClick={() => navigate('/recruiter/jobs')}>Back to Jobs</Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div className="min-w-0 flex-1">
          <button
            onClick={() => navigate('/recruiter/jobs')}
            className="text-body-sm text-neutral-500 hover:text-neutral-700 mb-2 inline-flex items-center gap-1"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <polyline points="15 18 9 12 15 6" />
            </svg>
            Back to Jobs
          </button>
          <div className="flex items-center gap-3 flex-wrap">
            <h2 className="text-heading-lg text-neutral-900 truncate">{job.title}</h2>
            <Badge variant={statusVariant[job.status]} size="sm" dot>{statusLabels[job.status]}</Badge>
          </div>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          {job.status === 'DRAFT' && (
            <>
              <Button variant="outline" size="sm" onClick={startEditing} disabled={actionLoading}>
                Edit
              </Button>
              <Button size="sm" onClick={handlePublish} loading={actionLoading}>
                Publish
              </Button>
              <Button variant="danger" size="sm" onClick={handleDelete} loading={actionLoading}>
                Delete
              </Button>
            </>
          )}
          {job.status === 'PUBLISHED' && (
            <>
              <Button variant="outline" size="sm" onClick={startEditing} disabled={actionLoading}>
                Edit
              </Button>
              <Button variant="danger" size="sm" onClick={handleClose} loading={actionLoading}>
                Close
              </Button>
            </>
          )}
        </div>
      </div>

      {editing ? (
        <form onSubmit={handleSaveEdit} className="space-y-6">
          <Card>
            <CardContent className="p-5 space-y-5">
              <h3 className="text-heading-sm text-neutral-900">Edit Job</h3>

              <Input
                label="Job Title"
                value={editForm.title || ''}
                onChange={(e) => setEditForm({ ...editForm, title: e.target.value })}
                error={editErrors.title}
                maxLength={200}
                required
              />

              <div>
                <label className="block text-label-md text-neutral-700 mb-1.5">Description</label>
                <textarea
                  className={`w-full rounded-lg border bg-white px-3.5 py-2 text-sm text-neutral-900 placeholder:text-neutral-400 transition-all duration-150 ease-in-out focus:outline-none focus:ring-2 focus:ring-offset-0 min-h-[200px] ${
                    editErrors.description
                      ? 'border-error-300 focus:border-error-500 focus:ring-error-500/20'
                      : 'border-neutral-300 focus:border-secondary-500 focus:ring-secondary-500/20'
                  }`}
                  value={editForm.description || ''}
                  onChange={(e) => setEditForm({ ...editForm, description: e.target.value })}
                  maxLength={10000}
                />
                <div className="flex items-center justify-between mt-1">
                  {editErrors.description ? (
                    <p className="text-body-sm text-error-600">{editErrors.description}</p>
                  ) : (
                    <span />
                  )}
                  <span className="text-body-sm text-neutral-400">{(editForm.description || '').length}/10,000</span>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Select
                  label="Employment Type"
                  options={employmentTypeOptions}
                  value={editForm.employmentType || ''}
                  onChange={(e) => setEditForm({ ...editForm, employmentType: e.target.value })}
                  error={editErrors.employmentType}
                />
                <Select
                  label="Workplace Type"
                  options={workplaceTypeOptions}
                  value={editForm.workplaceType || ''}
                  onChange={(e) => setEditForm({ ...editForm, workplaceType: e.target.value })}
                  error={editErrors.workplaceType}
                />
              </div>

              <Input
                label="Location"
                value={editForm.location || ''}
                onChange={(e) => setEditForm({ ...editForm, location: e.target.value })}
                maxLength={100}
              />
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-5 space-y-5">
              <h3 className="text-heading-sm text-neutral-900">Experience & Salary</h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Input
                  label="Min Experience (years)"
                  type="number"
                  min={0}
                  value={editForm.experienceMin ?? ''}
                  onChange={(e) => setEditForm({ ...editForm, experienceMin: e.target.value ? Number(e.target.value) : undefined })}
                  error={editErrors.experienceMin}
                />
                <Input
                  label="Max Experience (years)"
                  type="number"
                  min={0}
                  value={editForm.experienceMax ?? ''}
                  onChange={(e) => setEditForm({ ...editForm, experienceMax: e.target.value ? Number(e.target.value) : undefined })}
                  error={editErrors.experienceMax}
                />
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Input
                  label="Min Salary"
                  type="number"
                  min={0}
                  value={editForm.salaryMin ?? ''}
                  onChange={(e) => setEditForm({ ...editForm, salaryMin: e.target.value ? Number(e.target.value) : undefined })}
                  error={editErrors.salaryMin}
                />
                <Input
                  label="Max Salary"
                  type="number"
                  min={0}
                  value={editForm.salaryMax ?? ''}
                  onChange={(e) => setEditForm({ ...editForm, salaryMax: e.target.value ? Number(e.target.value) : undefined })}
                  error={editErrors.salaryMax}
                />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-5 space-y-5">
              <h3 className="text-heading-sm text-neutral-900">Additional Details</h3>
              <Input
                label="Skills"
                placeholder="e.g. React, TypeScript, Node.js (comma-separated)"
                value={editForm.skills || ''}
                onChange={(e) => setEditForm({ ...editForm, skills: e.target.value })}
                maxLength={1000}
                hint="Separate skills with commas"
              />
              <Input
                label="Application Deadline"
                type="datetime-local"
                value={editForm.applicationDeadline || ''}
                onChange={(e) => setEditForm({ ...editForm, applicationDeadline: e.target.value })}
              />
            </CardContent>
          </Card>

          <div className="flex items-center justify-end gap-3">
            <Button type="button" variant="outline" onClick={cancelEditing} disabled={saving}>
              Cancel
            </Button>
            <Button type="submit" loading={saving}>
              Save Changes
            </Button>
          </div>
        </form>
      ) : (
        <div className="space-y-6">
          <Card>
            <CardContent className="p-5 space-y-5">
              <h3 className="text-heading-sm text-neutral-900">Job Details</h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
                <DetailField label="Employment Type" value={employmentTypeLabels[job.employmentType]} />
                <DetailField label="Workplace Type" value={workplaceTypeLabels[job.workplaceType]} />
                <DetailField label="Location" value={job.location} />
                <DetailField
                  label="Experience"
                  value={
                    job.experienceMin != null && job.experienceMax != null
                      ? `${job.experienceMin} - ${job.experienceMax} years`
                      : job.experienceMin != null
                        ? `${job.experienceMin}+ years`
                        : job.experienceMax != null
                          ? `Up to ${job.experienceMax} years`
                          : null
                  }
                />
                <DetailField
                  label="Salary"
                  value={
                    job.salaryMin != null && job.salaryMax != null
                      ? `$${job.salaryMin.toLocaleString()} - $${job.salaryMax.toLocaleString()}`
                      : job.salaryMin != null
                        ? `$${job.salaryMin.toLocaleString()}+`
                        : job.salaryMax != null
                          ? `Up to $${job.salaryMax.toLocaleString()}`
                          : null
                  }
                />
                <DetailField label="Deadline" value={job.applicationDeadline ? formatDate(job.applicationDeadline) : null} />
              </div>
            </CardContent>
          </Card>

          {job.skills && (
            <Card>
              <CardContent className="p-5">
                <h3 className="text-heading-sm text-neutral-900 mb-3">Skills</h3>
                <div className="flex flex-wrap gap-2">
                  {job.skills.split(',').map((skill, i) => (
                    <span key={i} className="text-body-sm bg-neutral-100 text-neutral-700 px-3 py-1 rounded-full">
                      {skill.trim()}
                    </span>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}

          <Card>
            <CardContent className="p-5">
              <h3 className="text-heading-sm text-neutral-900 mb-3">Description</h3>
              <div className="text-body-md text-neutral-700 whitespace-pre-wrap leading-relaxed">
                {job.description}
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-5 space-y-3">
              <h3 className="text-heading-sm text-neutral-900">Timeline</h3>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-body-sm">
                <div>
                  <span className="text-neutral-500">Created</span>
                  <p className="font-medium text-neutral-900">{formatDateTime(job.createdAt)}</p>
                </div>
                <div>
                  <span className="text-neutral-500">Last Updated</span>
                  <p className="font-medium text-neutral-900">{formatDateTime(job.updatedAt)}</p>
                </div>
                {job.publishedAt && (
                  <div>
                    <span className="text-neutral-500">Published</span>
                    <p className="font-medium text-neutral-900">{formatDateTime(job.publishedAt)}</p>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>

          {job.status !== 'CLOSED' && (
            <div className="flex justify-center">
              <Button variant="outline" onClick={() => navigate('/recruiter/applications')}>
                View Applications
              </Button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
