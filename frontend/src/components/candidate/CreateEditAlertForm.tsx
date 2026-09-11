import { useState } from 'react';
import { Button, Input, Select } from '@/components/ui';
import type { JobAlert, CreateJobAlertRequest } from '@/services/jobAlert.service';

interface CreateEditAlertFormProps {
  initialData?: JobAlert | null;
  onSubmit: (data: CreateJobAlertRequest) => Promise<void>;
  onCancel: () => void;
}

const workplaceTypeOptions = [
  { value: '', label: 'Any workplace type' },
  { value: 'ONSITE', label: 'On-site' },
  { value: 'REMOTE', label: 'Remote' },
  { value: 'HYBRID', label: 'Hybrid' },
];

const employmentTypeOptions = [
  { value: '', label: 'Any employment type' },
  { value: 'FULL_TIME', label: 'Full-time' },
  { value: 'PART_TIME', label: 'Part-time' },
  { value: 'CONTRACT', label: 'Contract' },
  { value: 'INTERNSHIP', label: 'Internship' },
  { value: 'FREELANCE', label: 'Freelance' },
];

export default function CreateEditAlertForm({ initialData, onSubmit, onCancel }: CreateEditAlertFormProps) {
  const [formData, setFormData] = useState<CreateJobAlertRequest>({
    name: initialData?.name || '',
    keywords: initialData?.keywords || '',
    location: initialData?.location || '',
    workplaceType: initialData?.workplaceType || '',
    employmentType: initialData?.employmentType || '',
    minimumExperience: initialData?.minimumExperience,
    skills: initialData?.skills || '',
    active: initialData?.active ?? true,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);
  const [apiError, setApiError] = useState<string | null>(null);

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Alert name is required';
    } else if (formData.name.length > 100) {
      newErrors.name = 'Alert name must not exceed 100 characters';
    }

    if (formData.keywords && formData.keywords.length > 200) {
      newErrors.keywords = 'Keywords must not exceed 200 characters';
    }

    if (formData.location && formData.location.length > 100) {
      newErrors.location = 'Location must not exceed 100 characters';
    }

    if (formData.skills && formData.skills.length > 500) {
      newErrors.skills = 'Skills must not exceed 500 characters';
    }

    if (formData.minimumExperience !== undefined && formData.minimumExperience !== null && formData.minimumExperience < 0) {
      newErrors.minimumExperience = 'Experience must be 0 or greater';
    }

    const hasAtLeastOneCriterion =
      (formData.keywords && formData.keywords.trim()) ||
      (formData.location && formData.location.trim()) ||
      formData.workplaceType ||
      formData.employmentType ||
      (formData.minimumExperience !== undefined && formData.minimumExperience !== null) ||
      (formData.skills && formData.skills.trim());

    if (!hasAtLeastOneCriterion) {
      newErrors.general = 'At least one matching criterion must be provided';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setApiError(null);

    if (!validate()) return;

    setLoading(true);
    try {
      await onSubmit({
        ...formData,
        keywords: formData.keywords || undefined,
        location: formData.location || undefined,
        workplaceType: formData.workplaceType || undefined,
        employmentType: formData.employmentType || undefined,
        minimumExperience: formData.minimumExperience || undefined,
        skills: formData.skills || undefined,
      });
    } catch (err: any) {
      setApiError(err.response?.data?.message || 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {apiError && (
        <div className="rounded-lg bg-error-50 p-4 text-body-sm text-error-700">
          {apiError}
        </div>
      )}

      {errors.general && (
        <div className="rounded-lg bg-error-50 p-4 text-body-sm text-error-700">
          {errors.general}
        </div>
      )}

      <Input
        label="Alert Name"
        value={formData.name}
        onChange={e => setFormData(prev => ({ ...prev, name: e.target.value }))}
        error={errors.name}
        placeholder="e.g., Remote React Developer Jobs"
        required
      />

      <Input
        label="Keywords"
        value={formData.keywords}
        onChange={e => setFormData(prev => ({ ...prev, keywords: e.target.value }))}
        error={errors.keywords}
        placeholder="e.g., React, JavaScript, Frontend"
        hint="Comma-separated keywords to match in job titles and descriptions"
      />

      <Input
        label="Location"
        value={formData.location}
        onChange={e => setFormData(prev => ({ ...prev, location: e.target.value }))}
        error={errors.location}
        placeholder="e.g., New York, San Francisco"
      />

      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
        <Select
          label="Workplace Type"
          value={formData.workplaceType || ''}
          onChange={e => setFormData(prev => ({ ...prev, workplaceType: e.target.value || undefined }))}
          options={workplaceTypeOptions}
        />

        <Select
          label="Employment Type"
          value={formData.employmentType || ''}
          onChange={e => setFormData(prev => ({ ...prev, employmentType: e.target.value || undefined }))}
          options={employmentTypeOptions}
        />
      </div>

      <Input
        label="Minimum Experience (years)"
        type="number"
        min="0"
        value={formData.minimumExperience ?? ''}
        onChange={e => setFormData(prev => ({
          ...prev,
          minimumExperience: e.target.value ? parseInt(e.target.value) : undefined
        }))}
        error={errors.minimumExperience}
        placeholder="e.g., 3"
      />

      <Input
        label="Skills"
        value={formData.skills}
        onChange={e => setFormData(prev => ({ ...prev, skills: e.target.value }))}
        error={errors.skills}
        placeholder="e.g., React, TypeScript, Node.js"
        hint="Comma-separated skills to match"
      />

      <div className="flex items-center gap-3">
        <label className="relative inline-flex cursor-pointer items-center">
          <input
            type="checkbox"
            checked={formData.active}
            onChange={e => setFormData(prev => ({ ...prev, active: e.target.checked }))}
            className="peer sr-only"
          />
          <div className="h-6 w-11 rounded-full bg-neutral-200 transition-colors peer-checked:bg-primary-600" />
          <div className="absolute left-0.5 top-1/2 h-5 w-5 -translate-y-1/2 rounded-full bg-white shadow-sm transition-transform peer-checked:translate-x-5" />
        </label>
        <span className="text-sm text-neutral-700">Active</span>
      </div>

      <div className="flex justify-end gap-3 pt-4">
        <Button type="button" variant="outline" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" loading={loading}>
          {initialData ? 'Update Alert' : 'Create Alert'}
        </Button>
      </div>
    </form>
  );
}
