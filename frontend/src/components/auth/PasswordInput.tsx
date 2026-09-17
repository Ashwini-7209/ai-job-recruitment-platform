import { useId, useState } from 'react';

interface PasswordInputProps {
  label?: string;
  name: string;
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  onBlur?: () => void;
  error?: string;
  placeholder?: string;
  disabled?: boolean;
  autoComplete?: string;
  showStrength?: boolean;
  strength?: number;
}

function PasswordStrength({ strength }: { strength: number }) {
  const labels = ['Very weak', 'Weak', 'Fair', 'Strong', 'Very strong'];
  const colors = ['bg-error-500', 'bg-error-400', 'bg-warning-500', 'bg-success-400', 'bg-success-600'];
  const idx = Math.max(0, Math.min(4, strength - 1));

  if (strength === 0) return null;

  return (
    <div className="mt-2 space-y-1.5">
      <div className="flex gap-1">
        {Array.from({ length: 5 }).map((_, i) => (
          <div
            key={i}
            className={`h-1 flex-1 rounded-full transition-colors ${
              i < strength ? colors[idx] : 'bg-neutral-200'
            }`}
          />
        ))}
      </div>
      <p className={`text-caption ${strength <= 2 ? 'text-error-600' : strength <= 3 ? 'text-warning-600' : 'text-success-600'}`}>
        {labels[idx]}
      </p>
    </div>
  );
}

export function calculatePasswordStrength(password: string): number {
  if (!password) return 0;
  let score = 0;
  if (password.length >= 8) score++;
  if (password.length >= 12) score++;
  if (/[A-Z]/.test(password)) score++;
  if (/[0-9]/.test(password)) score++;
  if (/[^A-Za-z0-9]/.test(password)) score++;
  return Math.min(score, 5);
}

export default function PasswordInput({
  label = 'Password',
  name,
  value,
  onChange,
  onBlur,
  error,
  placeholder = 'Enter your password',
  disabled = false,
  autoComplete = 'current-password',
  showStrength = false,
  strength = 0,
}: PasswordInputProps) {
  const [visible, setVisible] = useState(false);
  const autoId = useId();
  const id = `pw-${name}-${autoId}`;

  return (
    <div className="w-full">
      {label && (
        <label htmlFor={id} className="block text-label-md text-neutral-700 mb-1.5">
          {label}
        </label>
      )}
      <div className="relative">
        <input
          id={id}
          name={name}
          type={visible ? 'text' : 'password'}
          value={value}
          onChange={onChange}
          onBlur={onBlur}
          placeholder={placeholder}
          disabled={disabled}
          autoComplete={autoComplete}
          className={[
            'block w-full rounded-lg border bg-white px-3.5 py-2 pr-10 text-sm text-neutral-900',
            'placeholder:text-neutral-400',
            'transition-colors duration-150 ease-in-out',
            'focus:outline-none focus:ring-2 focus:ring-offset-0',
            error
              ? 'border-error-300 focus:border-error-500 focus:ring-error-500/20'
              : 'border-neutral-300 focus:border-secondary-500 focus:ring-secondary-500/20',
            disabled ? 'bg-neutral-50 text-neutral-500 cursor-not-allowed' : '',
          ].join(' ')}
        />
        <button
          type="button"
          onClick={() => setVisible(!visible)}
          className="absolute inset-y-0 right-0 flex items-center pr-3 text-neutral-400 hover:text-neutral-600 transition-colors"
          tabIndex={-1}
          aria-label={visible ? 'Hide password' : 'Show password'}
        >
          {visible ? (
            <svg className="h-4.5 w-4.5" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" d="M3.98 8.223A10.477 10.477 0 0 0 1.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.451 10.451 0 0 1 12 4.5c4.756 0 8.773 3.162 10.065 7.498a10.522 10.522 0 0 1-4.293 5.774M6.228 6.228 3 3m3.228 3.228 3.65 3.65m7.894 7.894L21 21m-3.228-3.228-3.65-3.65m0 0a3 3 0 1 0-4.243-4.243m4.242 4.242L9.88 9.88" />
            </svg>
          ) : (
            <svg className="h-4.5 w-4.5" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" d="M2.036 12.322a1.012 1.012 0 0 1 0-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178Z" />
              <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z" />
            </svg>
          )}
        </button>
      </div>
      {error && <p className="mt-1 text-body-sm text-error-600">{error}</p>}
      {showStrength && !error && <PasswordStrength strength={strength} />}
    </div>
  );
}
