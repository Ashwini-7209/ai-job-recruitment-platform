import { useState, useEffect, useRef } from 'react';
import { Card, CardContent, Button, Badge, Skeleton, EmptyState } from '@/components/ui';
import { resumeService, type Resume } from '@/services/resume.service';

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

const ACCEPTED_TYPES = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
const MAX_SIZE = 10 * 1024 * 1024;

export default function CandidateResumePage() {
  const [resumes, setResumes] = useState<Resume[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadError, setUploadError] = useState('');
  const [uploadSuccess, setUploadSuccess] = useState('');
  const [actionError, setActionError] = useState('');
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    fetchResumes();
  }, []);

  const fetchResumes = async () => {
    try {
      const data = await resumeService.getResumes();
      setResumes(data);
    } catch {
      setActionError('Failed to load resumes. Please refresh the page.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploadError('');
    setUploadSuccess('');

    if (!ACCEPTED_TYPES.includes(file.type)) {
      setUploadError('Please upload a PDF, DOC, or DOCX file.');
      return;
    }
    if (file.size > MAX_SIZE) {
      setUploadError('File size must be less than 10 MB.');
      return;
    }

    setIsUploading(true);
    try {
      const newResume = await resumeService.uploadResume(file);
      setResumes((prev) => [newResume, ...prev]);
      setUploadSuccess('Resume uploaded successfully.');
      if (fileInputRef.current) fileInputRef.current.value = '';
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      setUploadError(error.response?.data?.message || 'Upload failed. Please try again.');
    } finally {
      setIsUploading(false);
    }
  };

  const handleActivate = async (resumeId: number) => {
    try {
      await resumeService.activateResume(resumeId);
      setResumes((prev) => prev.map((r) =>
        r.id === resumeId ? { ...r, active: true } : { ...r, active: false }
      ));
    } catch {
      setActionError('Failed to activate resume. Please try again.');
    }
  };

  const handleDelete = async (resumeId: number) => {
    if (!confirm('Are you sure you want to delete this resume?')) return;
    try {
      await resumeService.deleteResume(resumeId);
      setResumes((prev) => prev.filter((r) => r.id !== resumeId));
    } catch {
      setActionError('Failed to delete resume. Please try again.');
    }
  };

  if (isLoading) {
    return (
      <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
        <Skeleton className="h-8 w-48" />
        <Skeleton className="h-40" />
        {Array.from({ length: 2 }).map((_, i) => (
          <Skeleton key={i} className="h-24" />
        ))}
      </div>
    );
  }

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
      <div>
        <h1 className="text-heading-lg text-neutral-900">Resume</h1>
        <p className="text-body-md text-neutral-500 mt-1">Upload and manage your resume for job applications.</p>
      </div>

      {/* Upload Area */}
      <Card>
        <CardContent className="p-5">
          <div
            className="border-2 border-dashed border-neutral-200 rounded-xl p-8 text-center hover:border-secondary-300 transition-colors cursor-pointer"
            onClick={() => fileInputRef.current?.click()}
          >
            <input
              ref={fileInputRef}
              type="file"
              accept=".pdf,.doc,.docx"
              onChange={handleUpload}
              className="hidden"
            />
            <div className="flex flex-col items-center gap-3">
              <div className="flex h-12 w-12 items-center justify-center rounded-full bg-secondary-50 text-secondary-600">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                  <polyline points="17,8 12,3 7,8" />
                  <line x1="12" y1="3" x2="12" y2="15" />
                </svg>
              </div>
              <div>
                <p className="text-body-md font-medium text-neutral-900">
                  {isUploading ? 'Uploading...' : 'Click to upload your resume'}
                </p>
                <p className="text-body-sm text-neutral-500 mt-1">PDF, DOC, or DOCX (max 10 MB)</p>
              </div>
            </div>
          </div>

          {uploadError && (
            <div className="mt-4 rounded-lg bg-error-50 border border-error-200 p-3 text-body-sm text-error-700">
              {uploadError}
            </div>
          )}
          {uploadSuccess && (
            <div className="mt-4 rounded-lg bg-success-50 border border-success-200 p-3 text-body-sm text-success-700">
              {uploadSuccess}
            </div>
          )}
          {actionError && (
            <div className="mt-4 rounded-lg bg-error-50 border border-error-200 p-3 text-body-sm text-error-700">
              {actionError}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Resume List */}
      {resumes.length === 0 ? (
        <Card>
          <CardContent className="p-0">
            <EmptyState
              icon={
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <polyline points="14,2 14,8 20,8" />
                </svg>
              }
              title="No resumes uploaded"
              description="Upload your resume to apply for jobs and help recruiters find you."
            />
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-3">
          {resumes.map((resume) => (
            <Card key={resume.id}>
              <CardContent className="p-4 sm:p-5">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                  <div className="flex items-start gap-3 min-w-0">
                    <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-secondary-50 text-secondary-600">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                        <polyline points="14,2 14,8 20,8" />
                      </svg>
                    </div>
                    <div className="min-w-0">
                      <p className="text-body-md font-medium text-neutral-900 truncate">{resume.originalFileName}</p>
                      <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-body-sm text-neutral-500 mt-0.5">
                        <span>{formatFileSize(resume.fileSize)}</span>
                        <span>{resume.contentType.split('/').pop()?.toUpperCase()}</span>
                        <span>Uploaded {formatDate(resume.createdAt)}</span>
                      </div>
                    </div>
                  </div>
                  <div className="flex items-center gap-2 shrink-0">
                    {resume.active ? (
                      <Badge variant="success" size="sm">Active</Badge>
                    ) : (
                      <Button variant="ghost" size="sm" onClick={() => handleActivate(resume.id)}>
                        Set Active
                      </Button>
                    )}
                    <button
                      onClick={() => resumeService.downloadResume(resume.id, resume.originalFileName)}
                      className="inline-flex items-center justify-center rounded-lg border border-neutral-200 px-3 py-1.5 text-body-sm font-medium text-neutral-700 hover:bg-neutral-50 transition-colors"
                    >
                      Download
                    </button>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="text-error-500 hover:text-error-600 hover:bg-error-50"
                      onClick={() => handleDelete(resume.id)}
                    >
                      Delete
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
