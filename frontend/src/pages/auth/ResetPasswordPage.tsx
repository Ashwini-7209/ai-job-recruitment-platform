import { useState, useEffect, type FormEvent } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { AuthLayout, PasswordInput, AlertMessage } from '@/components/auth';
import { Button } from '@/components/ui';
import { calculatePasswordStrength } from '@/components/auth/PasswordInput';
import passwordService from '@/services/password.service';

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
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [form, setForm] = useState<Form>({ password: '', confirmPassword: '' });
  const [errors, setErrors] = useState<FormErrors>({});
  const [submitted, setSubmitted] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [serverError, setServerError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [tokenError, setTokenError] = useState('');

  useEffect(() => {
    if (!token) {
      setTokenError('No reset token found. Please request a new password reset link.');
    }
  }, [token]);

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

    if (!token) {
      setTokenError('No reset token found. Please request a new password reset link.');
      return;
    }

    const newErrors = validate(form);
    setErrors(newErrors);
    if (Object.values(newErrors).some(Boolean)) return;

    setIsSubmitting(true);
    setSubmitted(true);
    try {
      const response = await passwordService.resetPassword({
        token,
        newPassword: form.password,
        confirmPassword: form.confirmPassword,
      });

      if (response.success) {
        setSuccessMessage('Password reset successful. Please log in with your new password.');
        setTimeout(() => navigate('/login'), 2000);
      } else {
        setServerError(response.message || 'Unable to reset your password. The link may have expired.');
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
            <h1 className="text-heading-lg text-neutral-900">Password reset successfully</h1>
            <p className="mt-2 text-body-md text-neutral-500">
              {successMessage}
            </p>
          </div>
          <Button fullWidth onClick={() => navigate('/login')}>
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

        {tokenError && <AlertMessage type="error" message={tokenError} />}
        {serverError && <AlertMessage type="error" message={serverError} />}

        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <PasswordInput
            name="newPassword"
            label="New password"
            value={form.password}
            onChange={handleChange('password')}
            error={errors.password}
            placeholder="At least 8 characters"
            disabled={isSubmitting || !!tokenError}
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
            disabled={isSubmitting || !!tokenError}
            autoComplete="new-password"
          />

          <Button type="submit" fullWidth loading={isSubmitting} disabled={isSubmitting || !!tokenError}>
            Reset password
          </Button>
        </form>

        <p className="text-center text-body-md text-neutral-500">
          <Link to="/login" className="font-medium text-secondary-600 hover:text-secondary-700 transition-colors">
            Back to sign in
          </Link>
        </p>
      </div>
    </AuthLayout>
  );
}
