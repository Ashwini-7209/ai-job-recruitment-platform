import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, Button, Input, Select } from '@/components/ui';
import { recruiterJobService, type CreateJobData } from '@/services/recruiterJob.service';

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

interface FormErrors {
  title?: string;
  description?: string;
  employmentType?: string;
  workplaceType?: string;
  experienceMin?: string;
  experienceMax?: string;
  salaryMin?: string;
  salaryMax?: string;
}

export default function RecruiterJobCreatePage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [aiLoading, setAiLoading] = useState(false);
  const [descriptionAiGenerated, setDescriptionAiGenerated] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [errors, setErrors] = useState<FormErrors>({});

  const [form, setForm] = useState<CreateJobData>({
    title: '',
    description: '',
    location: '',
    employmentType: '',
    workplaceType: '',
    experienceMin: undefined,
    experienceMax: undefined,
    salaryMin: undefined,
    salaryMax: undefined,
    skills: '',
    applicationDeadline: '',
  });

  const updateField = <K extends keyof CreateJobData>(key: K, value: CreateJobData[K]) => {
    setForm((prev) => ({ ...prev, [key]: value }));
    if (errors[key as keyof FormErrors]) {
      setErrors((prev) => ({ ...prev, [key]: undefined }));
    }
  };

  const validate = (): boolean => {
    const newErrors: FormErrors = {};

    if (!form.title.trim()) {
      newErrors.title = 'Title is required.';
    } else if (form.title.length > 200) {
      newErrors.title = 'Title must be 200 characters or less.';
    }

    if (!form.description.trim()) {
      newErrors.description = 'Description is required.';
    } else if (form.description.length > 10000) {
      newErrors.description = 'Description must be 10,000 characters or less.';
    }

    if (!form.employmentType) {
      newErrors.employmentType = 'Employment type is required.';
    }

    if (!form.workplaceType) {
      newErrors.workplaceType = 'Workplace type is required.';
    }

    if (form.experienceMin !== undefined && form.experienceMin < 0) {
      newErrors.experienceMin = 'Must be 0 or greater.';
    }

    if (form.experienceMax !== undefined && form.experienceMax < 0) {
      newErrors.experienceMax = 'Must be 0 or greater.';
    }

    if (
      form.experienceMin !== undefined &&
      form.experienceMax !== undefined &&
      form.experienceMin > form.experienceMax
    ) {
      newErrors.experienceMax = 'Must be greater than or equal to minimum.';
    }

    if (form.salaryMin !== undefined && form.salaryMin < 0) {
      newErrors.salaryMin = 'Must be 0 or greater.';
    }

    if (form.salaryMax !== undefined && form.salaryMax < 0) {
      newErrors.salaryMax = 'Must be 0 or greater.';
    }

    if (
      form.salaryMin !== undefined &&
      form.salaryMax !== undefined &&
      form.salaryMin > form.salaryMax
    ) {
      newErrors.salaryMax = 'Must be greater than or equal to minimum.';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    try {
      setLoading(true);
      setError(null);
      const payload: CreateJobData = {
        title: form.title.trim(),
        description: form.description.trim(),
        employmentType: form.employmentType,
        workplaceType: form.workplaceType,
      };

      if (form.location?.trim()) payload.location = form.location.trim();
      if (form.experienceMin !== undefined) payload.experienceMin = form.experienceMin;
      if (form.experienceMax !== undefined) payload.experienceMax = form.experienceMax;
      if (form.salaryMin !== undefined) payload.salaryMin = form.salaryMin;
      if (form.salaryMax !== undefined) payload.salaryMax = form.salaryMax;
      if (form.skills?.trim()) payload.skills = form.skills.trim();
      if (form.applicationDeadline) payload.applicationDeadline = new Date(form.applicationDeadline).toISOString();

      const job = await recruiterJobService.createJob(payload);
      navigate(`/recruiter/jobs/${job.id}`);
    } catch {
      setError('Failed to create job. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateDescription = async () => {
    try {
      setAiLoading(true);
      const description = await recruiterJobService.generateJobDescription({
        title: form.title || undefined,
        skills: form.skills || undefined,
        employmentType: form.employmentType || undefined,
        workplaceType: form.workplaceType || undefined,
        experienceMin: form.experienceMin,
        experienceMax: form.experienceMax,
      });
      updateField('description', description);
      setDescriptionAiGenerated(true);
    } catch {
      setError('Unable to generate description. Please write manually.');
    } finally {
      setAiLoading(false);
    }
  };

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
      <div>
        <h2 className="text-heading-lg text-neutral-900">Create Job</h2>
        <p className="text-body-md text-neutral-500 mt-1">Fill in the details to create a new job posting</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {error && (
          <Card>
            <CardContent className="p-4">
              <p className="text-body-sm text-error-600">{error}</p>
            </CardContent>
          </Card>
        )}

        <Card>
          <CardContent className="p-5 space-y-5">
            <h3 className="text-heading-sm text-neutral-900">Basic Information</h3>

            <Input
              label="Job Title"
              placeholder="e.g. Senior Frontend Developer"
              value={form.title}
              onChange={(e) => updateField('title', e.target.value)}
              error={errors.title}
              maxLength={200}
              required
            />

            <div>
              <div className="flex items-center justify-between mb-1.5">
                <label className="block text-label-md text-neutral-700">Description</label>
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  loading={aiLoading}
                  onClick={handleGenerateDescription}
                  icon={
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M12 3l1.912 5.813a2 2 0 001.275 1.275L21 12l-5.813 1.912a2 2 0 00-1.275 1.275L12 21l-1.912-5.813a2 2 0 00-1.275-1.275L3 12l5.813-1.912a2 2 0 001.275-1.275L12 3z" />
                    </svg>
                  }
                >
                  Generate with AI
                </Button>
              </div>
              <textarea
                className={`w-full rounded-lg border bg-white px-3.5 py-2 text-sm text-neutral-900 placeholder:text-neutral-400 transition-all duration-150 ease-in-out focus:outline-none focus:ring-2 focus:ring-offset-0 min-h-[200px] ${
                  errors.description
                    ? 'border-error-300 focus:border-error-500 focus:ring-error-500/20'
                    : 'border-neutral-300 focus:border-secondary-500 focus:ring-secondary-500/20'
                }`}
                placeholder="Describe the role, responsibilities, and requirements..."
                value={form.description}
                onChange={(e) => { updateField('description', e.target.value); setDescriptionAiGenerated(false); }}
                maxLength={10000}
              />
              <div className="flex items-center justify-between mt-1">
                {errors.description ? (
                  <p className="text-body-sm text-error-600">{errors.description}</p>
                ) : (
                  <span />
                )}
                <span className="text-body-sm text-neutral-400">{form.description.length}/10,000</span>
              </div>
              {descriptionAiGenerated && form.description && (
                <p className="text-body-xs text-neutral-400 mt-1 italic">
                  AI-generated content - please review and edit before publishing
                </p>
              )}
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Select
                label="Employment Type"
                options={employmentTypeOptions}
                placeholder="Select type"
                value={form.employmentType}
                onChange={(e) => updateField('employmentType', e.target.value)}
                error={errors.employmentType}
              />
              <Select
                label="Workplace Type"
                options={workplaceTypeOptions}
                placeholder="Select type"
                value={form.workplaceType}
                onChange={(e) => updateField('workplaceType', e.target.value)}
                error={errors.workplaceType}
              />
            </div>

            <Input
              label="Location"
              placeholder="e.g. New York, NY or Remote"
              value={form.location || ''}
              onChange={(e) => updateField('location', e.target.value)}
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
                placeholder="0"
                value={form.experienceMin ?? ''}
                onChange={(e) => updateField('experienceMin', e.target.value ? Number(e.target.value) : undefined)}
                error={errors.experienceMin}
              />
              <Input
                label="Max Experience (years)"
                type="number"
                min={0}
                placeholder="e.g. 5"
                value={form.experienceMax ?? ''}
                onChange={(e) => updateField('experienceMax', e.target.value ? Number(e.target.value) : undefined)}
                error={errors.experienceMax}
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Input
                label="Min Salary"
                type="number"
                min={0}
                placeholder="e.g. 50000"
                value={form.salaryMin ?? ''}
                onChange={(e) => updateField('salaryMin', e.target.value ? Number(e.target.value) : undefined)}
                error={errors.salaryMin}
              />
              <Input
                label="Max Salary"
                type="number"
                min={0}
                placeholder="e.g. 80000"
                value={form.salaryMax ?? ''}
                onChange={(e) => updateField('salaryMax', e.target.value ? Number(e.target.value) : undefined)}
                error={errors.salaryMax}
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
              value={form.skills || ''}
              onChange={(e) => updateField('skills', e.target.value)}
              maxLength={1000}
              hint="Separate skills with commas"
            />

            <Input
              label="Application Deadline"
              type="datetime-local"
              value={form.applicationDeadline || ''}
              onChange={(e) => updateField('applicationDeadline', e.target.value)}
            />
          </CardContent>
        </Card>

        <div className="flex items-center justify-end gap-3">
          <Button type="button" variant="outline" onClick={() => navigate(-1)} disabled={loading}>
            Cancel
          </Button>
          <Button type="submit" loading={loading}>
            Create Job
          </Button>
        </div>
      </form>
    </div>
  );
}
