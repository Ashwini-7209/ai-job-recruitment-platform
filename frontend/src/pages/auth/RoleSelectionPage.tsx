import { useState } from 'react';
import { Link } from 'react-router-dom';
import { AuthLayout } from '@/components/auth';
import { Button } from '@/components/ui';
import type { AuthRole } from '@/types/auth';

const roles: { value: AuthRole; title: string; description: string; icon: React.ReactNode }[] = [
  {
    value: 'CANDIDATE',
    title: 'Candidate',
    description: 'Find your next opportunity. Get matched with roles that fit your skills, experience, and career goals.',
    icon: (
      <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.501 20.118a7.5 7.5 0 0 1 14.998 0A17.933 17.933 0 0 1 12 21.75c-2.676 0-5.216-.584-7.499-1.632Z" />
      </svg>
    ),
  },
  {
    value: 'RECRUITER',
    title: 'Recruiter',
    description: 'Post jobs, source candidates, and manage your hiring pipeline with AI-powered tools.',
    icon: (
      <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" d="M20.25 14.15v4.25c0 1.094-.787 2.036-1.872 2.18-2.087.277-4.216.42-6.378.42s-4.291-.143-6.378-.42c-1.085-.144-1.872-1.086-1.872-2.18v-4.25m16.5 0a2.18 2.18 0 0 0 .75-1.661V8.706c0-1.081-.768-2.015-1.837-2.175a48.114 48.114 0 0 0-3.413-.387m4.5 8.006c-.194.165-.42.295-.673.38A23.978 23.978 0 0 1 12 15.75c-2.648 0-5.195-.429-7.577-1.22a2.016 2.016 0 0 1-.673-.38m0 0A2.18 2.18 0 0 1 3 12.489V8.706c0-1.081.768-2.015 1.837-2.175a48.111 48.111 0 0 1 3.413-.387m7.5 0V5.25A2.25 2.25 0 0 0 13.5 3h-3a2.25 2.25 0 0 0-2.25 2.25v.894m7.5 0a48.667 48.667 0 0 0-7.5 0" />
      </svg>
    ),
  },
];

export default function RoleSelectionPage() {
  const [selected, setSelected] = useState<AuthRole | null>(null);

  return (
    <AuthLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-heading-lg text-neutral-900">Create your account</h1>
          <p className="mt-1 text-body-md text-neutral-500">
            Choose how you'd like to use JobRecruit
          </p>
        </div>

        <div className="space-y-3" role="radiogroup" aria-label="Select account type">
          {roles.map((role) => {
            const isSelected = selected === role.value;
            return (
              <button
                key={role.value}
                type="button"
                role="radio"
                aria-checked={isSelected}
                onClick={() => setSelected(role.value)}
                className={[
                  'w-full text-left rounded-xl p-5 border-2 transition-all duration-150',
                  'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600',
                  isSelected
                    ? 'border-primary-500 bg-primary-50 ring-1 ring-primary-500'
                    : 'border-neutral-200 bg-white hover:border-neutral-300 hover:bg-neutral-50',
                ].join(' ')}
              >
                <div className="flex items-start gap-4">
                  <div className={[
                    'flex h-12 w-12 shrink-0 items-center justify-center rounded-xl transition-colors',
                    isSelected ? 'bg-primary-600 text-white' : 'bg-neutral-100 text-neutral-500',
                  ].join(' ')}>
                    {role.icon}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2.5">
                      <h3 className="text-heading-sm text-neutral-900">{role.title}</h3>
                      {isSelected && (
                        <svg className="h-4 w-4 shrink-0 text-primary-600" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" d="m4.5 12.75 6 6 9-13.5" />
                        </svg>
                      )}
                    </div>
                    <p className="mt-1 text-body-sm text-neutral-500">{role.description}</p>
                  </div>
                </div>
              </button>
            );
          })}
        </div>

        <Button
          fullWidth
          size="lg"
          disabled={!selected}
          onClick={() => {
            if (selected) {
              window.location.href = selected === 'CANDIDATE' ? '/register/candidate' : '/register/recruiter';
            }
          }}
        >
          Continue as {selected ? (selected === 'CANDIDATE' ? 'Candidate' : 'Recruiter') : '...'}
        </Button>

        <p className="text-center text-body-md text-neutral-500">
          Already have an account?{' '}
          <Link to="/login" className="font-medium text-primary-600 hover:text-primary-700 transition-colors">
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
