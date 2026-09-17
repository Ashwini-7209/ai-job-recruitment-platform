import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthLayout, PasswordInput, AlertMessage } from '@/components/auth';
import { Button } from '@/components/ui';
import { calculatePasswordStrength } from '@/components/auth/PasswordInput';
import { useAuth } from '@/contexts';
import passwordService from '@/services/password.service';
import type { AuthRole } from '@/types/auth';

const profileRedirectMap: Record<AuthRole, string> = {
  CANDIDATE: '/candidate/profile',
  RECRUITER: '/recruiter/profile',
  ADMIN: '/admin/settings',
};

interface Form {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

interface FormErrors {
  currentPassword?: string;
  newPassword?: string;
  confirmPassword?: string;
}

function validate(data: Form): FormErrors {
  const errors: FormErrors = {};
  if (!data.currentPassword) errors.currentPassword = 'Current password is required';
  if (!data.newPassword) errors.newPassword = 'New password is required';
  else if (data.newPassword.length < 8) errors.newPassword = 'Password must be at least 8 characters';
  if (!data.confirmPassword) errors.confirmPassword = 'Please confirm your password';
  else if (data.newPassword !== data.confirmPassword) errors.confirmPassword = 'Passwords do not match';
  if (data.currentPassword && data.newPassword && data.currentPassword === data.newPassword) {
    errors.newPassword = 'New password must be different from current password';
  }
  return errors;
}

export default function ChangePasswordPage() {
  const navigate = useNavigate();
  const { logout, user } = useAuth();
  const [form, setForm] = useState<Form>({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [errors, setErrors] = useState<FormErrors>({});
  const [submitted, setSubmitted] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [serverError, setServerError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const handleChange = (field: keyof Form) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm((prev) => ({ ...prev, [field]: e.target.value }));
    if (submitted) {
      const newForm = { ...form, [field]: e.target.value };
      const newErrors = validate(newForm);
      setErrors((prev) => ({ ...prev, [field]: newErrors[field] }));
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setSubmitted(true);
    setServerError('');

    const newErrors = validate(form);
    setErrors(newErrors);
    if (Object.values(newErrors).some(Boolean)) return;

    setIsSubmitting(true);
    try {
      const response = await passwordService.changePassword({
        currentPassword: form.currentPassword,
        newPassword: form.newPassword,
        confirmPassword: form.confirmPassword,
      });

      if (response.success) {
        setSuccessMessage('Password changed successfully. Please log in with your new password.');
        setTimeout(() => {
          logout();
          navigate('/login');
        }, 2000);
      } else {
        setServerError(response.message || 'Unable to change password. Please try again.');
      }
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } };
      const backendMessage = axiosError?.response?.data?.message;
      if (backendMessage) {
        setServerError(backendMessage);
      } else {
        setServerError('Unable to connect to the server. Please try again later.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  if (successMessage) {
    return (
      <AuthLayout>
        <div className="space-y-6 text-center">
          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-success-100">
            <svg className="h-7 w-7 text-success-600" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" d="m4.5 12.75 6 6 9-13.5" />
            </svg>
          </div>
          <div>
            <h1 className="text-heading-lg text-neutral-900">Password changed</h1>
            <p className="mt-2 text-body-md text-neutral-500">
              {successMessage}
            </p>
          </div>
          <Button fullWidth onClick={() => { logout(); navigate('/login'); }}>
            Sign in with new password
          </Button>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-heading-lg text-neutral-900">Change your password</h1>
          <p className="mt-1 text-body-md text-neutral-500">
            Enter your current password and choose a new one
          </p>
        </div>

        {serverError && <AlertMessage type="error" message={serverError} />}

        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <PasswordInput
            name="currentPassword"
            label="Current password"
            value={form.currentPassword}
            onChange={handleChange('currentPassword')}
            error={errors.currentPassword}
            placeholder="Enter your current password"
            disabled={isSubmitting}
            autoComplete="current-password"
          />

          <PasswordInput
            name="newPassword"
            label="New password"
            value={form.newPassword}
            onChange={handleChange('newPassword')}
            error={errors.newPassword}
            placeholder="At least 8 characters"
            disabled={isSubmitting}
            autoComplete="new-password"
            showStrength
            strength={calculatePasswordStrength(form.newPassword)}
          />

          <PasswordInput
            name="confirmPassword"
            label="Confirm new password"
            value={form.confirmPassword}
            onChange={handleChange('confirmPassword')}
            error={errors.confirmPassword}
            placeholder="Re-enter your new password"
            disabled={isSubmitting}
            autoComplete="new-password"
          />

          <Button type="submit" fullWidth loading={isSubmitting} disabled={isSubmitting}>
            Change password
          </Button>
        </form>

        <p className="text-center text-body-md text-neutral-500">
          <Link to={user?.role ? profileRedirectMap[user.role] : '/login'} className="font-medium text-secondary-600 hover:text-secondary-700 transition-colors">
            Back to profile
          </Link>
        </p>
      </div>
    </AuthLayout>
  );
}
