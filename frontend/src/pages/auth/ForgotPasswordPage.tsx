import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthLayout, AlertMessage } from '@/components/auth';
import { Button, Input } from '@/components/ui';
import passwordService from '@/services/password.service';

export default function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [submitted, setSubmitted] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [serverError, setServerError] = useState('');

  const validate = (): string | undefined => {
    if (!email) return 'Email is required';
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return 'Please enter a valid email address';
    return undefined;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setServerError('');

    const err = validate();
    setError(err || '');
    if (err) return;

    setIsSubmitting(true);
    try {
      await passwordService.forgotPassword(email);
      setSubmitted(true);
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

  if (submitted) {
    return (
      <AuthLayout>
        <div className="space-y-6 text-center">
          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-secondary-50">
            <svg className="h-7 w-7 text-secondary-700" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 0 1-2.25 2.25h-15a2.25 2.25 0 0 1-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0 0 19.5 4.5h-15a2.25 2.25 0 0 0-2.25 2.25m19.5 0v.243a2.25 2.25 0 0 1-1.07 1.916l-7.5 4.615a2.25 2.25 0 0 1-2.36 0L3.32 8.91a2.25 2.25 0 0 1-1.07-1.916V6.75" />
            </svg>
          </div>
          <div>
            <h1 className="text-heading-lg text-neutral-900">Check your email</h1>
            <p className="mt-2 text-body-md text-neutral-500 max-w-sm mx-auto">
              If an account exists for <strong className="text-neutral-700">{email}</strong>, we've sent instructions to reset your password.
            </p>
          </div>
          <Button variant="outline" fullWidth onClick={() => navigate('/login')}>
            Back to sign in
          </Button>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-heading-lg text-neutral-900">Forgot your password?</h1>
          <p className="mt-1 text-body-md text-neutral-500">
            Enter your email and we'll send you a link to reset your password
          </p>
        </div>

        {serverError && <AlertMessage type="error" message={serverError} />}

        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <Input
            label="Email address"
            type="email"
            value={email}
            onChange={(e) => { setEmail(e.target.value); if (error) setError(''); }}
            error={error}
            placeholder="you@example.com"
            autoComplete="email"
            disabled={isSubmitting}
          />

          <Button type="submit" fullWidth loading={isSubmitting} disabled={isSubmitting}>
            Send reset link
          </Button>
        </form>

        <p className="text-center text-body-md text-neutral-500">
          Remember your password?{' '}
          <Link to="/login" className="font-medium text-secondary-600 hover:text-secondary-700 transition-colors">
            Sign in
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
