import { type ReactNode } from 'react';
import { Link } from 'react-router-dom';

interface AuthLayoutProps {
  children: ReactNode;
}

function BrandPanel() {
  return (
    <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-primary-900 via-primary-800 to-primary-950 relative overflow-hidden">
      <div className="absolute inset-0 opacity-10">
        <div className="absolute top-20 left-20 h-72 w-72 rounded-full bg-secondary-400 blur-3xl" />
        <div className="absolute bottom-20 right-20 h-96 w-96 rounded-full bg-secondary-400 blur-3xl" />
      </div>
      <div className="relative z-10 flex flex-col justify-between p-12 xl:p-16">
        <Link to="/" className="flex items-center gap-2.5" aria-label="HireFlow Home">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-white/15 backdrop-blur-sm">
            <svg className="h-5 w-5 text-white" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" d="M20.25 14.15v4.25c0 1.094-.787 2.036-1.872 2.18-2.087.277-4.216.42-6.378.42s-4.291-.143-6.378-.42c-1.085-.144-1.872-1.086-1.872-2.18v-4.25m16.5 0a2.18 2.18 0 0 0 .75-1.661V8.706c0-1.081-.768-2.015-1.837-2.175a48.114 48.114 0 0 0-3.413-.387m4.5 8.006c-.194.165-.42.295-.673.38A23.978 23.978 0 0 1 12 15.75c-2.648 0-5.195-.429-7.577-1.22a2.016 2.016 0 0 1-.673-.38m0 0A2.18 2.18 0 0 1 3 12.489V8.706c0-1.081.768-2.015 1.837-2.175a48.111 48.111 0 0 1 3.413-.387m7.5 0V5.25A2.25 2.25 0 0 0 13.5 3h-3a2.25 2.25 0 0 0-2.25 2.25v.894m7.5 0a48.667 48.667 0 0 0-7.5 0" />
            </svg>
          </div>
          <span className="text-xl font-bold text-white">HireFlow</span>
        </Link>

        <div className="space-y-6">
          <h1 className="text-heading-xl font-bold text-white">
            AI-Powered Hiring
            <br />
            <span className="text-secondary-300">Made Simple</span>
          </h1>
          <p className="text-body-lg text-primary-200 max-w-md">
            Streamline your recruitment with intelligent matching, automated screening, and data-driven hiring decisions.
          </p>
          <div className="space-y-3">
            {[
              'AI-powered candidate matching',
              'Automated resume analysis',
              'Real-time interview insights',
            ].map((feature) => (
              <div key={feature} className="flex items-center gap-2.5 text-primary-200">
                <svg className="h-4 w-4 shrink-0 text-secondary-400" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" d="m4.5 12.75 6 6 9-13.5" />
                </svg>
                <span className="text-sm">{feature}</span>
              </div>
            ))}
          </div>
        </div>

        <p className="text-caption text-primary-300/60">
          &copy; {new Date().getFullYear()} HireFlow. All rights reserved.
        </p>
      </div>
    </div>
  );
}

export default function AuthLayout({ children }: AuthLayoutProps) {
  return (
    <div className="flex min-h-screen bg-white">
      <BrandPanel />
      <div className="flex w-full lg:w-1/2 flex-col">
        <div className="flex-1 flex items-center justify-center px-4 sm:px-6 lg:px-8 py-12">
          <div className="w-full max-w-[400px]">
            {/* Mobile logo */}
            <div className="lg:hidden mb-8 text-center">
              <Link to="/" className="inline-flex items-center gap-2.5" aria-label="HireFlow Home">
                <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-secondary-500">
                  <svg className="h-4.5 w-4.5 text-white" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M20.25 14.15v4.25c0 1.094-.787 2.036-1.872 2.18-2.087.277-4.216.42-6.378.42s-4.291-.143-6.378-.42c-1.085-.144-1.872-1.086-1.872-2.18v-4.25m16.5 0a2.18 2.18 0 0 0 .75-1.661V8.706c0-1.081-.768-2.015-1.837-2.175a48.114 48.114 0 0 0-3.413-.387m4.5 8.006c-.194.165-.42.295-.673.38A23.978 23.978 0 0 1 12 15.75c-2.648 0-5.195-.429-7.577-1.22a2.016 2.016 0 0 1-.673-.38m0 0A2.18 2.18 0 0 1 3 12.489V8.706c0-1.081.768-2.015 1.837-2.175a48.111 48.111 0 0 1 3.413-.387m7.5 0V5.25A2.25 2.25 0 0 0 13.5 3h-3a2.25 2.25 0 0 0-2.25 2.25v.894m7.5 0a48.667 48.667 0 0 0-7.5 0" />
                  </svg>
                </div>
                <span className="text-lg font-bold text-primary-900">HireFlow</span>
              </Link>
            </div>
            {children}
          </div>
        </div>
      </div>
    </div>
  );
}
