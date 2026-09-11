import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { AuthLayout, PasswordInput, AlertMessage } from '@/components/auth';
import { Button } from '@/components/ui';
import { calculatePasswordStrength } from '@/components/auth/PasswordInput';

interface Form {
  password: string;
  confirmPassword: string;
}

interface FormErrors {
  password?: string;
  confirmPassword?: string;
}

function validate(data: Form): FormErrors {
  const errors: FormErrors = {};
  if (!data.password) errors.password = 'New password is required';
  else if (data.password.length < 8) errors.password = 'Password must be at least 8 characters';
  if (!data.confirmPassword) errors.confirmPassword = 'Please confirm your password';
  else if (data.password !== data.confirmPassword) errors.confirmPassword = 'Passwords do not match';
  return errors;
}

export default function ResetPasswordPage() {
  const [form, setForm] = useState<Form>({ password: '', confirmPassword: '' });
  const [errors, setErrors] = useState<FormErrors>({});
  const [submitted, setSubmitted] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [serverError, setServerError] = useState('');

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
    setServerError('');

    const newErrors = validate(form);
    setErrors(newErrors);
    if (Object.values(newErrors).some(Boolean)) return;

    setIsSubmitting(true);
    try {
      setSubmitted(true);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'An unexpected error occurred';
      if (message.includes('Not implemented')) {
        setSubmitted(true);
      } else {
        setServerError('Unable to reset your password. The link may have expired.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  if (submitted) {
    return (
      <AuthLayout>
        <div className="space-y-6 text-center">
          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-success-100">
            <svg className="h-7 w-7 text-success-600" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" d="m4.5 12.75 6 6 9-13.5" />
            </svg>
          </div>
          <div>
            <h1 className="text-heading-lg text-neutral-900">Password reset successfully</h1>
            <p className="mt-2 text-body-md text-neutral-500">
              Your password has been updated. You can now sign in with your new password.
            </p>
          </div>
          <AlertMessage
            type="info"
            message="Password reset is not yet connected to the backend. This is a placeholder confirmation screen."
          />
          <Button fullWidth onClick={() => window.location.href = '/login'}>
            Sign in
          </Button>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-heading-lg text-neutral-900">Reset your password</h1>
          <p className="mt-1 text-body-md text-neutral-500">
            Enter your new password below
          </p>
        </div>

        {serverError && <AlertMessage type="error" message={serverError} />}

        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <PasswordInput
            name="newPassword"
            value={form.password}
            onChange={handleChange('password')}
            error={errors.password}
            placeholder="At least 8 characters"
            disabled={isSubmitting}
            autoComplete="new-password"
            showStrength
            strength={calculatePasswordStrength(form.password)}
          />

          <PasswordInput
            name="confirmPassword"
            label="Confirm password"
            value={form.confirmPassword}
            onChange={handleChange('confirmPassword')}
            error={errors.confirmPassword}
            placeholder="Re-enter your password"
            disabled={isSubmitting}
            autoComplete="new-password"
          />

          <Button type="submit" fullWidth loading={isSubmitting} disabled={isSubmitting}>
            Reset password
          </Button>
        </form>

        <p className="text-center text-body-md text-neutral-500">
          <Link to="/login" className="font-medium text-primary-600 hover:text-primary-700 transition-colors">
            Back to sign in
          </Link>
        </p>
      </div>
    </AuthLayout>
  );
}
