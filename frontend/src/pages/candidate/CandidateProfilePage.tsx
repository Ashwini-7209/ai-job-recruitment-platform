import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Card, CardContent, Button, Input, Skeleton } from '@/components/ui';
import { profileService, type CandidateProfile, type ProfileCompletion } from '@/services/profile.service';
import { useAuth } from '@/contexts';

const MAX_LENGTHS = {
  phone: 20,
  location: 100,
  headline: 100,
  bio: 2000,
  currentJobTitle: 100,
  educationSummary: 1000,
  skillsSummary: 2000,
  linkedinUrl: 255,
  githubUrl: 255,
  portfolioUrl: 255,
};

export default function CandidateProfilePage() {
  const { user } = useAuth();
  const [, setProfile] = useState<CandidateProfile | null>(null);
  const [completion, setCompletion] = useState<ProfileCompletion | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);
  const [saveError, setSaveError] = useState('');
  const [form, setForm] = useState({
    phone: '',
    location: '',
    headline: '',
    bio: '',
    currentJobTitle: '',
    yearsOfExperience: '',
    educationSummary: '',
    skillsSummary: '',
    linkedinUrl: '',
    githubUrl: '',
    portfolioUrl: '',
  });

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const [profileData, completionData] = await Promise.all([
          profileService.getProfile(),
          profileService.getProfileCompletion(),
        ]);
        setProfile(profileData);
        setCompletion(completionData);
        setForm({
          phone: profileData.phone || '',
          location: profileData.location || '',
          headline: profileData.headline || '',
          bio: profileData.bio || '',
          currentJobTitle: profileData.currentJobTitle || '',
          yearsOfExperience: profileData.yearsOfExperience?.toString() || '',
          educationSummary: profileData.educationSummary || '',
          skillsSummary: profileData.skillsSummary || '',
          linkedinUrl: profileData.linkedinUrl || '',
          githubUrl: profileData.githubUrl || '',
          portfolioUrl: profileData.portfolioUrl || '',
        });
      } catch {
        setSaveError('Failed to load profile. Please refresh the page.');
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
      const updateData: Record<string, unknown> = {};
      Object.entries(form).forEach(([key, value]) => {
        if (key === 'yearsOfExperience') {
          updateData[key] = value ? parseInt(value, 10) : null;
        } else {
          updateData[key] = value || null;
        }
      });
      const updated = await profileService.updateProfile(updateData as Record<string, string | number | null>);
      setProfile(updated);
      const newCompletion = await profileService.getProfileCompletion();
      setCompletion(newCompletion);
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
        <h1 className="text-heading-lg text-neutral-900">My Profile</h1>
        <p className="text-body-md text-neutral-500 mt-1">Manage your personal information and professional details.</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <form onSubmit={handleSubmit} className="lg:col-span-2 space-y-6">
          {/* Basic Information */}
          <Card>
            <CardContent className="p-5 space-y-4">
              <h3 className="text-heading-sm text-neutral-900">Basic Information</h3>
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
                  <label className="text-body-sm font-medium text-neutral-700 mb-1 block">Location</label>
                  <Input
                    placeholder="e.g. San Francisco, CA"
                    value={form.location}
                    onChange={(e) => handleChange('location', e.target.value)}
                    maxLength={MAX_LENGTHS.location}
                  />
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Professional Information */}
          <Card>
            <CardContent className="p-5 space-y-4">
              <h3 className="text-heading-sm text-neutral-900">Professional Information</h3>
              <div>
                <label className="text-body-sm font-medium text-neutral-700 mb-1 block">Professional Headline</label>
                <Input
                  placeholder="e.g. Senior Software Engineer"
                  value={form.headline}
                  onChange={(e) => handleChange('headline', e.target.value)}
                  maxLength={MAX_LENGTHS.headline}
                />
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="text-body-sm font-medium text-neutral-700 mb-1 block">Current Job Title</label>
                  <Input
                    placeholder="e.g. Software Engineer"
                    value={form.currentJobTitle}
                    onChange={(e) => handleChange('currentJobTitle', e.target.value)}
                    maxLength={MAX_LENGTHS.currentJobTitle}
                  />
                </div>
                <div>
                  <label className="text-body-sm font-medium text-neutral-700 mb-1 block">Years of Experience</label>
                  <Input
                    type="number"
                    min="0"
                    max="50"
                    placeholder="e.g. 5"
                    value={form.yearsOfExperience}
                    onChange={(e) => handleChange('yearsOfExperience', e.target.value)}
                  />
                </div>
              </div>
              <div>
                <label className="text-body-sm font-medium text-neutral-700 mb-1 block">About / Bio</label>
                <textarea
                  className="w-full rounded-lg border border-neutral-200 px-3 py-2 text-body-md focus:outline-none focus:ring-2 focus:ring-secondary-500 focus:border-transparent resize-none"
                  rows={4}
                  placeholder="Tell us about yourself..."
                  value={form.bio}
                  onChange={(e) => handleChange('bio', e.target.value)}
                  maxLength={MAX_LENGTHS.bio}
                />
                <p className="text-caption text-neutral-400 mt-1">{form.bio.length}/{MAX_LENGTHS.bio}</p>
              </div>
            </CardContent>
          </Card>

          {/* Skills & Education */}
          <Card>
            <CardContent className="p-5 space-y-4">
              <h3 className="text-heading-sm text-neutral-900">Skills & Education</h3>
              <div>
                <label className="text-body-sm font-medium text-neutral-700 mb-1 block">Skills</label>
                <textarea
                  className="w-full rounded-lg border border-neutral-200 px-3 py-2 text-body-md focus:outline-none focus:ring-2 focus:ring-secondary-500 focus:border-transparent resize-none"
                  rows={3}
                  placeholder="e.g. JavaScript, React, Node.js, Python, SQL"
                  value={form.skillsSummary}
                  onChange={(e) => handleChange('skillsSummary', e.target.value)}
                  maxLength={MAX_LENGTHS.skillsSummary}
                />
              </div>
              <div>
                <label className="text-body-sm font-medium text-neutral-700 mb-1 block">Education</label>
                <textarea
                  className="w-full rounded-lg border border-neutral-200 px-3 py-2 text-body-md focus:outline-none focus:ring-2 focus:ring-secondary-500 focus:border-transparent resize-none"
                  rows={3}
                  placeholder="e.g. B.S. Computer Science, Stanford University, 2018"
                  value={form.educationSummary}
                  onChange={(e) => handleChange('educationSummary', e.target.value)}
                  maxLength={MAX_LENGTHS.educationSummary}
                />
              </div>
            </CardContent>
          </Card>

          {/* Links */}
          <Card>
            <CardContent className="p-5 space-y-4">
              <h3 className="text-heading-sm text-neutral-900">Online Presence</h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="text-body-sm font-medium text-neutral-700 mb-1 block">LinkedIn URL</label>
                  <Input
                    placeholder="https://linkedin.com/in/yourprofile"
                    value={form.linkedinUrl}
                    onChange={(e) => handleChange('linkedinUrl', e.target.value)}
                    maxLength={MAX_LENGTHS.linkedinUrl}
                  />
                </div>
                <div>
                  <label className="text-body-sm font-medium text-neutral-700 mb-1 block">GitHub URL</label>
                  <Input
                    placeholder="https://github.com/yourusername"
                    value={form.githubUrl}
                    onChange={(e) => handleChange('githubUrl', e.target.value)}
                    maxLength={MAX_LENGTHS.githubUrl}
                  />
                </div>
              </div>
              <div>
                <label className="text-body-sm font-medium text-neutral-700 mb-1 block">Portfolio URL</label>
                <Input
                  placeholder="https://yourportfolio.com"
                  value={form.portfolioUrl}
                  onChange={(e) => handleChange('portfolioUrl', e.target.value)}
                  maxLength={MAX_LENGTHS.portfolioUrl}
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

        {/* Completion Sidebar */}
        <div className="space-y-6">
          <Card>
            <CardContent className="p-5">
              <h3 className="text-heading-sm text-neutral-900 mb-3">Profile Completion</h3>
              <div className="flex items-center justify-between mb-2">
                <span className="text-body-sm text-neutral-500">Progress</span>
                <span className="text-heading-sm text-secondary-600">{completion?.overallPercentage || 0}%</span>
              </div>
              <div className="w-full h-2.5 bg-neutral-100 rounded-full overflow-hidden mb-4">
                <div
                  className="h-full bg-secondary-500 rounded-full transition-all duration-500"
                  style={{ width: `${completion?.overallPercentage || 0}%` }}
                />
              </div>
              <div className="space-y-3">
                {completion?.sections.map((section) => (
                  <div key={section.section}>
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2 text-body-sm">
                        <span className={section.completed ? 'text-success-500' : 'text-neutral-300'}>
                          {section.completed ? (
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                              <polyline points="20,6 9,17 4,12" />
                            </svg>
                          ) : (
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                              <circle cx="12" cy="12" r="10" />
                            </svg>
                          )}
                        </span>
                        <span className={section.completed ? 'text-neutral-600' : 'text-neutral-400'}>{section.label}</span>
                      </div>
                      <span className="text-caption text-neutral-400">{section.weight}%</span>
                    </div>
                    {!section.completed && section.missingFields.length > 0 && (
                      <p className="text-caption text-neutral-400 ml-6 mt-0.5">
                        Missing: {section.missingFields.map(f => f.replace(/([A-Z])/g, ' $1').replace(/^./, s => s.toUpperCase())).join(', ')}
                      </p>
                    )}
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

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
              <h3 className="text-heading-sm text-neutral-900 mb-2">Tips</h3>
              <ul className="space-y-2 text-body-sm text-neutral-500">
                <li className="flex items-start gap-2">
                  <span className="text-secondary-400 mt-1">-</span>
                  Add your phone number so recruiters can reach you.
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-secondary-400 mt-1">-</span>
                  A professional headline helps you stand out.
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-secondary-400 mt-1">-</span>
                  Upload an active resume to apply for jobs.
                </li>
              </ul>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
