import { Link } from 'react-router-dom';
import { Button, Badge } from '@/components/ui';

export default function HeroSection() {
  return (
    <section className="relative overflow-hidden bg-gradient-to-b from-secondary-50/50 via-white to-neutral-50">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 pt-12 sm:pt-16 lg:pt-24 pb-12 sm:pb-16 lg:pb-28">
        <div className="grid lg:grid-cols-2 gap-8 lg:gap-16 items-center">
          {/* Left content */}
          <div className="max-w-2xl">
            <Badge variant="primary" size="md" className="mb-4 sm:mb-5">
              AI-Powered Recruitment Platform
            </Badge>
            <h1 className="text-display-lg text-primary-900">
              Smarter hiring starts with{' '}
              <span className="text-secondary-600">better data</span>
            </h1>
            <p className="mt-4 sm:mt-5 text-body-lg text-neutral-600 max-w-lg">
              JobRecruit connects talented candidates with the right opportunities using AI-driven matching,
              resume analysis, and intelligent recruitment workflows. Build your career or find your next great hire.
            </p>
            <div className="mt-6 sm:mt-8 flex flex-col sm:flex-row gap-3">
              <Link to="/register/candidate">
                <Button size="lg" variant="primary" className="w-full sm:w-auto">
                  Find Your Next Role
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none" className="ml-1">
                    <path d="M3 8h10M9 4l4 4-4 4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                </Button>
              </Link>
              <Link to="/register/recruiter">
                <Button size="lg" variant="outline" className="w-full sm:w-auto">
                  Post a Job
                </Button>
              </Link>
            </div>
            <div className="mt-6 sm:mt-8 flex flex-col sm:flex-row sm:items-center gap-3 sm:gap-6 text-sm text-neutral-500">
              <div className="flex items-center gap-2">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none" className="text-secondary-500 shrink-0">
                  <path d="M13.3 4.7L6 12 2.7 8.7" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
                <span>AI resume analysis</span>
              </div>
              <div className="flex items-center gap-2">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none" className="text-secondary-500 shrink-0">
                  <path d="M13.3 4.7L6 12 2.7 8.7" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
                <span>Smart job matching</span>
              </div>
              <div className="flex items-center gap-2">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none" className="text-secondary-500 shrink-0">
                  <path d="M13.3 4.7L6 12 2.7 8.7" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
                <span>Free to start</span>
              </div>
            </div>
          </div>

          {/* Right - Dashboard mockup */}
          <div className="relative hidden lg:block">
            <div className="relative rounded-xl bg-white ring-1 ring-neutral-200/80 shadow-lg overflow-hidden">
              {/* Mockup header */}
              <div className="flex items-center gap-2 px-4 py-3 border-b border-neutral-100 bg-neutral-50/50">
                <div className="flex gap-1.5">
                  <div className="h-3 w-3 rounded-full bg-neutral-300" />
                  <div className="h-3 w-3 rounded-full bg-neutral-300" />
                  <div className="h-3 w-3 rounded-full bg-neutral-300" />
                </div>
                <div className="ml-4 flex-1 h-6 rounded-md bg-white ring-1 ring-neutral-200 flex items-center px-3">
                  <span className="text-xs text-neutral-400">jobrecruit.com/dashboard</span>
                </div>
              </div>
              {/* Mockup body */}
              <div className="p-5 space-y-4">
                {/* Stats row */}
                <div className="grid grid-cols-3 gap-3">
                  {[
                    { label: 'Applications', value: '24', change: '+8 this week' },
                    { label: 'Profile Views', value: '156', change: '+23 this week' },
                    { label: 'Match Score', value: '89%', change: 'Top 5%' },
                  ].map((stat) => (
                    <div key={stat.label} className="rounded-lg bg-neutral-50 p-3 ring-1 ring-neutral-100">
                      <p className="text-xs text-neutral-500">{stat.label}</p>
                      <p className="text-lg font-semibold text-primary-900 mt-0.5">{stat.value}</p>
                      <p className="text-xs text-secondary-600 mt-0.5">{stat.change}</p>
                    </div>
                  ))}
                </div>
                {/* Job matches */}
                <div className="rounded-lg bg-neutral-50 p-4 ring-1 ring-neutral-100">
                  <div className="flex items-center justify-between mb-3">
                    <p className="text-sm font-medium text-primary-900">Top Job Matches</p>
                    <span className="text-xs text-secondary-600 font-medium">View all</span>
                  </div>
                  <div className="space-y-2.5">
                    {[
                      { title: 'Senior Frontend Developer', company: 'TechCorp', match: '95%' },
                      { title: 'Full Stack Engineer', company: 'StartupXYZ', match: '91%' },
                      { title: 'React Developer', company: 'DigitalAgency', match: '87%' },
                    ].map((job, i) => (
                      <div key={i} className="flex items-center justify-between rounded-md bg-white p-2.5 ring-1 ring-neutral-200">
                        <div>
                          <p className="text-sm font-medium text-primary-900">{job.title}</p>
                          <p className="text-xs text-neutral-500">{job.company}</p>
                        </div>
                        <span className="text-xs font-medium text-secondary-600 bg-secondary-50 px-2 py-0.5 rounded-full">{job.match}</span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
            {/* Decorative elements */}
            <div className="absolute -bottom-4 -left-4 h-24 w-24 rounded-full bg-secondary-100/50 blur-2xl" />
            <div className="absolute -top-4 -right-4 h-20 w-20 rounded-full bg-secondary-100/50 blur-2xl" />
          </div>
        </div>
      </div>
    </section>
  );
}
