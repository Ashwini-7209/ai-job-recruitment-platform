import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Card, CardContent, Button, Input, Skeleton } from '@/components/ui';
import { recruiterProfileService } from '@/services/recruiterProfile.service';
import { useAuth } from '@/contexts';

const MAX_LENGTHS = {
  phone: 20,
  jobTitle: 100,
  department: 100,
  companyName: 200,
  companyWebsite: 255,
  companyDescription: 2000,
  companyLocation: 200,
  linkedinUrl: 255,
};

export default function RecruiterProfilePage() {
  const { user } = useAuth();
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);
  const [saveError, setSaveError] = useState('');
  const [form, setForm] = useState({
    phone: '',
    jobTitle: '',
    department: '',
    companyName: '',
    companyWebsite: '',
    companyDescription: '',
    companyLocation: '',
    linkedinUrl: '',
  });

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await recruiterProfileService.getProfile();
        setForm({
          phone: data.phone || '',
          jobTitle: data.jobTitle || '',
          department: data.department || '',
          companyName: data.companyName || '',
          companyWebsite: data.companyWebsite || '',
          companyDescription: data.companyDescription || '',
          companyLocation: data.companyLocation || '',
          linkedinUrl: data.linkedinUrl || '',
        });
      } catch {
        // silently fail
      } finally {
        setIsLoading(false);
      }
    };
    fetchProfile();
  }, []);

  const handleChange = (field: string, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    setSaveSuccess(false);
    setSaveError('');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaving(true);
    setSaveSuccess(false);
    setSaveError('');
    try {
      const updateData: Record<string, string | null> = {};
      Object.entries(form).forEach(([key, value]) => {
        updateData[key] = value || null;
      });
      await recruiterProfileService.updateProfile(updateData);
      setSaveSuccess(true);
      setTimeout(() => setSaveSuccess(false), 3000);
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      setSaveError(error.response?.data?.message || 'Failed to update profile. Please try again.');
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) {
    return (
      <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
        <Skeleton className="h-8 w-48" />
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 space-y-4">
            {Array.from({ length: 6 }).map((_, i) => (
              <Skeleton key={i} className="h-20" />
            ))}
          </div>
          <Skeleton className="h-48" />
        </div>
      </div>
    );
  }

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
      <div>
        <h1 className="text-heading-lg text-neutral-900">Company Profile</h1>
        <p className="text-body-md text-neutral-500 mt-1">Manage your company information and recruiter details.</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <form onSubmit={handleSubmit} className="lg:col-span-2 space-y-6">
          {/* Personal Information */}
          <Card>
            <CardContent className="p-5 space-y-4">
              <h3 className="text-heading-sm text-neutral-900">Personal Information</h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="text-body-sm font-medium text-neutral-700 mb-1 block">Full Name</label>
                  <Input value={user?.fullName || ''} disabled className="bg-neutral-50" />
                </div>
                <div>
                  <label className="text-body-sm font-medium text-neutral-700 mb-1 block">Email</label>
                  <Input value={user?.email || ''} disabled className="bg-neutral-50" />
                </div>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="text-body-sm font-medium text-neutral-700 mb-1 block">Phone</label>
                  <Input
                    placeholder="e.g. +1 (555) 123-4567"
                    value={form.phone}
                    onChange={(e) => handleChange('phone', e.target.value)}
                    maxLength={MAX_LENGTHS.phone}
                  />
                </div>
                <div>
                  <label className="text-body-sm font-medium text-neutral-700 mb-1 block">Job Title</label>
                  <Input
                    placeholder="e.g. Talent Acquisition Manager"
                    value={form.jobTitle}
                    onChange={(e) => handleChange('jobTitle', e.target.value)}
                    maxLength={MAX_LENGTHS.jobTitle}
                  />
                </div>
              </div>
              <div>
                <label className="text-body-sm font-medium text-neutral-700 mb-1 block">Department</label>
                <Input
                  placeholder="e.g. Human Resources"
                  value={form.department}
                  onChange={(e) => handleChange('department', e.target.value)}
                  maxLength={MAX_LENGTHS.department}
                />
              </div>
            </CardContent>
          </Card>

          {/* Company Information */}
          <Card>
            <CardContent className="p-5 space-y-4">
              <h3 className="text-heading-sm text-neutral-900">Company Information</h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="text-body-sm font-medium text-neutral-700 mb-1 block">Company Name</label>
                  <Input
                    placeholder="e.g. Acme Corporation"
                    value={form.companyName}
                    onChange={(e) => handleChange('companyName', e.target.value)}
                    maxLength={MAX_LENGTHS.companyName}
                  />
                </div>
                <div>
                  <label className="text-body-sm font-medium text-neutral-700 mb-1 block">Company Website</label>
                  <Input
                    placeholder="https://www.example.com"
                    value={form.companyWebsite}
                    onChange={(e) => handleChange('companyWebsite', e.target.value)}
                    maxLength={MAX_LENGTHS.companyWebsite}
                  />
                </div>
              </div>
              <div>
                <label className="text-body-sm font-medium text-neutral-700 mb-1 block">Company Location</label>
                <Input
                  placeholder="e.g. San Francisco, CA"
                  value={form.companyLocation}
                  onChange={(e) => handleChange('companyLocation', e.target.value)}
                  maxLength={MAX_LENGTHS.companyLocation}
                />
              </div>
              <div>
                <label className="text-body-sm font-medium text-neutral-700 mb-1 block">Company Description</label>
                <textarea
                  className="w-full rounded-lg border border-neutral-200 px-3 py-2 text-body-md focus:outline-none focus:ring-2 focus:ring-secondary-500 focus:border-transparent resize-none"
                  rows={4}
                  placeholder="Tell candidates about your company..."
                  value={form.companyDescription}
                  onChange={(e) => handleChange('companyDescription', e.target.value)}
                  maxLength={MAX_LENGTHS.companyDescription}
                />
                <p className="text-caption text-neutral-400 mt-1">{form.companyDescription.length}/{MAX_LENGTHS.companyDescription}</p>
              </div>
            </CardContent>
          </Card>

          {/* Links */}
          <Card>
            <CardContent className="p-5 space-y-4">
              <h3 className="text-heading-sm text-neutral-900">Online Presence</h3>
              <div>
                <label className="text-body-sm font-medium text-neutral-700 mb-1 block">LinkedIn URL</label>
                <Input
                  placeholder="https://linkedin.com/company/yourcompany"
                  value={form.linkedinUrl}
                  onChange={(e) => handleChange('linkedinUrl', e.target.value)}
                  maxLength={MAX_LENGTHS.linkedinUrl}
                />
              </div>
            </CardContent>
          </Card>

          {/* Feedback */}
          {saveSuccess && (
            <div className="rounded-lg bg-success-50 border border-success-200 p-4 text-body-sm text-success-700">
              Profile updated successfully.
            </div>
          )}
          {saveError && (
            <div className="rounded-lg bg-error-50 border border-error-200 p-4 text-body-sm text-error-700">
              {saveError}
            </div>
          )}

          <div className="flex justify-end">
            <Button type="submit" disabled={isSaving}>
              {isSaving ? 'Saving...' : 'Save Changes'}
            </Button>
          </div>
        </form>

        {/* Tips Sidebar */}
        <div className="space-y-6">
          <Card>
            <CardContent className="p-5">
              <h3 className="text-heading-sm text-neutral-900 mb-2">Security</h3>
              <p className="text-body-sm text-neutral-500 mb-3">Manage your account security settings.</p>
              <Link
                to="/change-password"
                className="inline-flex items-center gap-2 text-body-sm font-medium text-secondary-600 hover:text-secondary-700 transition-colors"
              >
                <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M16.5 10.5V6.75a4.5 4.5 0 1 0-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 0 0 2.25-2.25v-6.75a2.25 2.25 0 0 0-2.25-2.25H6.75a2.25 2.25 0 0 0-2.25 2.25v6.75a2.25 2.25 0 0 0 2.25 2.25Z" />
                </svg>
                Change Password
              </Link>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-5">
              <h3 className="text-heading-sm text-neutral-900 mb-3">Profile Status</h3>
              <div className="space-y-2.5">
                {[
                  { label: 'Phone number', completed: !!form.phone },
                  { label: 'Job title', completed: !!form.jobTitle },
                  { label: 'Company name', completed: !!form.companyName },
                  { label: 'Company description', completed: !!form.companyDescription },
                  { label: 'LinkedIn profile', completed: !!form.linkedinUrl },
                ].map((field) => (
                  <div key={field.label} className="flex items-center gap-2.5 text-body-sm">
                    <span className={field.completed ? 'text-success-500' : 'text-neutral-300'}>
                      {field.completed ? (
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                          <polyline points="20,6 9,17 4,12" />
                        </svg>
                      ) : (
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <circle cx="12" cy="12" r="10" />
                        </svg>
                      )}
                    </span>
                    <span className={field.completed ? 'text-neutral-600' : 'text-neutral-400'}>{field.label}</span>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-5">
              <h3 className="text-heading-sm text-neutral-900 mb-2">Tips</h3>
              <ul className="space-y-2 text-body-sm text-neutral-500">
                <li className="flex items-start gap-2">
                  <span className="text-secondary-400 mt-1">-</span>
                  Complete your company profile to attract more candidates.
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-secondary-400 mt-1">-</span>
                  A detailed company description builds trust with applicants.
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-secondary-400 mt-1">-</span>
                  Keep your contact information up to date for better communication.
                </li>
              </ul>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
