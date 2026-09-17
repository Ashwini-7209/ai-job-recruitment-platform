import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthLayout, PasswordInput, AlertMessage } from '@/components/auth';
import { Button, Input } from '@/components/ui';
import { calculatePasswordStrength } from '@/components/auth/PasswordInput';
import authService from '@/services/auth.service';

interface Form {
  fullName: string;
  email: string;
  password: string;
  confirmPassword: string;
  companyName: string;
}

interface FormErrors {
  fullName?: string;
  email?: string;
  password?: string;
  confirmPassword?: string;
  companyName?: string;
}

function validate(data: Form): FormErrors {
  const errors: FormErrors = {};
  if (!data.fullName.trim()) errors.fullName = 'Full name is required';
  if (!data.email) errors.email = 'Work email is required';
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) errors.email = 'Please enter a valid email address';
  if (!data.password) errors.password = 'Password is required';
  else if (data.password.length < 8) errors.password = 'Password must be at least 8 characters';
  if (!data.confirmPassword) errors.confirmPassword = 'Please confirm your password';
  else if (data.password !== data.confirmPassword) errors.confirmPassword = 'Passwords do not match';
  if (!data.companyName.trim()) errors.companyName = 'Company name is required';
  return errors;
}

export default function RegisterRecruiterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState<Form>({ fullName: '', email: '', password: '', confirmPassword: '', companyName: '' });
  const [errors, setErrors] = useState<FormErrors>({});
  const [submitted, setSubmitted] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [serverError, setServerError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [successMessage, setSuccessMessage] = useState('');

  const handleChange = (field: keyof Form) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm((prev) => ({ ...prev, [field]: e.target.value }));
    if (submitted) {
      const newForm = { ...form, [field]: e.target.value };
      const newErrors = validate(newForm);
      setErrors((prev) => ({ ...prev, [field]: newErrors[field] }));
    }
    if (fieldErrors[field]) {
      setFieldErrors((prev) => {
        const next = { ...prev };
        delete next[field];
        return next;
      });
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setSubmitted(true);
    setServerError('');
    setFieldErrors({});

    const newErrors = validate(form);
    setErrors(newErrors);
    if (Object.values(newErrors).some(Boolean)) return;

    setIsSubmitting(true);
    try {
      const response = await authService.registerRecruiter({
        fullName: form.fullName,
        email: form.email,
        password: form.password,
        confirmPassword: form.confirmPassword,
      });

      if (response.success) {
        setSuccessMessage('Account created successfully! You can now sign in.');
        setTimeout(() => navigate('/login'), 2000);
      } else {
        setServerError(response.message || 'Unable to create your account. Please try again.');
      }
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string; data?: Record<string, string> } } };
      const backendMessage = axiosError?.response?.data?.message;
      const backendFieldErrors = axiosError?.response?.data?.data;

      if (backendFieldErrors && typeof backendFieldErrors === 'object') {
        setFieldErrors(backendFieldErrors);
        setServerError('Please correct the errors below.');
      } else if (backendMessage) {
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
            <h1 className="text-heading-lg text-neutral-900">Account created!</h1>
            <p className="mt-2 text-body-md text-neutral-500">
              {successMessage}
            </p>
          </div>
          <Button fullWidth onClick={() => navigate('/login')}>
            Go to sign in
          </Button>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-heading-lg text-neutral-900">Create your account</h1>
          <p className="mt-1 text-body-md text-neutral-500">
            Join as a recruiter to start hiring smarter
          </p>
        </div>

        {serverError && <AlertMessage type="error" message={serverError} />}

        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <Input
            label="Full name"
            type="text"
            value={form.fullName}
            onChange={handleChange('fullName')}
            error={errors.fullName || fieldErrors.fullName}
            placeholder="Jane Smith"
            autoComplete="name"
            disabled={isSubmitting}
          />

          <Input
            label="Work email"
            type="email"
            value={form.email}
            onChange={handleChange('email')}
            error={errors.email || fieldErrors.email}
            placeholder="jane@company.com"
            autoComplete="email"
            disabled={isSubmitting}
          />

          <Input
            label="Company name"
            type="text"
            value={form.companyName}
            onChange={handleChange('companyName')}
            error={errors.companyName}
            placeholder="Acme Corp"
            autoComplete="organization"
            disabled={isSubmitting}
          />

          <PasswordInput
            name="password"
            value={form.password}
            onChange={handleChange('password')}
            error={errors.password || fieldErrors.password}
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
            error={errors.confirmPassword || fieldErrors.confirmPassword}
            placeholder="Re-enter your password"
            disabled={isSubmitting}
            autoComplete="new-password"
          />

          <Button type="submit" fullWidth loading={isSubmitting} disabled={isSubmitting}>
            Create recruiter account
          </Button>
        </form>

        <p className="text-center text-body-md text-neutral-500">
          Already have an account?{' '}
          <Link to="/login" className="font-medium text-secondary-600 hover:text-secondary-700 transition-colors">
            Sign in
          </Link>
        </p>

        <p className="text-center">
          <Link to="/register" className="text-body-sm text-neutral-400 hover:text-neutral-600 transition-colors">
            &larr; Choose a different role
          </Link>
        </p>
      </div>
    </AuthLayout>
  );
}
