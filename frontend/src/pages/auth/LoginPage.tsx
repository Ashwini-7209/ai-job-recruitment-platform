import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthLayout, PasswordInput, AlertMessage } from '@/components/auth';
import { Button, Input } from '@/components/ui';
import { useAuth } from '@/contexts';
import authService from '@/services/auth.service';
import type { AuthRole } from '@/types/auth';

interface LoginForm {
  email: string;
  password: string;
}

interface FormErrors {
  email?: string;
  password?: string;
}

function validateEmail(email: string): string | undefined {
  if (!email) return 'Email is required';
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return 'Please enter a valid email address';
  return undefined;
}

function validatePassword(password: string): string | undefined {
  if (!password) return 'Password is required';
  return undefined;
}

const roleRedirectMap: Record<AuthRole, string> = {
  CANDIDATE: '/candidate/dashboard',
  RECRUITER: '/recruiter/dashboard',
  ADMIN: '/admin/dashboard',
};

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [form, setForm] = useState<LoginForm>({ email: '', password: '' });
  const [errors, setErrors] = useState<FormErrors>({});
  const [submitted, setSubmitted] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [serverError, setServerError] = useState('');

  const handleChange = (field: keyof LoginForm) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm((prev) => ({ ...prev, [field]: e.target.value }));
    if (submitted) {
      setErrors((prev) => ({
        ...prev,
        [field]: field === 'email' ? validateEmail(e.target.value) : validatePassword(e.target.value),
      }));
    }
  };

  const validate = (): boolean => {
    const newErrors: FormErrors = {
      email: validateEmail(form.email),
      password: validatePassword(form.password),
    };
    setErrors(newErrors);
    return !newErrors.email && !newErrors.password;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setSubmitted(true);
    setServerError('');

    if (!validate()) return;

    setIsSubmitting(true);
    try {
      const response = await authService.login({
        email: form.email,
        password: form.password,
      });

      if (response.success && response.data) {
        const { accessToken, id, fullName, email, role } = response.data;
        login(accessToken, { id, fullName, email, role });
        navigate(roleRedirectMap[role]);
      } else {
        setServerError(response.message || 'Unable to sign in. Please try again.');
      }
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string; success?: boolean } } };
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

  return (
    <AuthLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-heading-lg text-neutral-900">Welcome back</h1>
          <p className="mt-1 text-body-md text-neutral-500">
            Sign in to your account to continue
          </p>
        </div>

        {serverError && <AlertMessage type="error" message={serverError} />}

        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <Input
            label="Email address"
            type="email"
            value={form.email}
            onChange={handleChange('email')}
            error={errors.email}
            placeholder="you@example.com"
            autoComplete="email"
            disabled={isSubmitting}
          />

          <PasswordInput
            name="password"
            value={form.password}
            onChange={handleChange('password')}
            error={errors.password}
            placeholder="Enter your password"
            disabled={isSubmitting}
            autoComplete="current-password"
          />

          <div className="flex items-center justify-end">
            <Link
              to="/forgot-password"
              className="text-body-sm font-medium text-secondary-600 hover:text-secondary-700 transition-colors"
            >
              Forgot password?
            </Link>
          </div>

          <Button
            type="submit"
            fullWidth
            loading={isSubmitting}
            disabled={isSubmitting}
          >
            Sign in
          </Button>
        </form>

        <p className="text-center text-body-md text-neutral-500">
          Don't have an account?{' '}
          <Link to="/register" className="font-medium text-secondary-600 hover:text-secondary-700 transition-colors">
            Get started
          </Link>
        </p>

        <p className="text-center">
          <Link to="/" className="text-body-sm text-neutral-400 hover:text-neutral-600 transition-colors">
            &larr; Back to home
          </Link>
        </p>
      </div>
    </AuthLayout>
  );
}
