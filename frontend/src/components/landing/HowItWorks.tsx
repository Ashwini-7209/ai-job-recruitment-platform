import { useState } from 'react';

const candidateSteps = [
  {
    step: '01',
    title: 'Create your profile',
    description: 'Sign up and build your professional profile. Add your experience, skills, and career preferences.',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.501 20.118a7.5 7.5 0 0 1 14.998 0" />
      </svg>
    ),
  },
  {
    step: '02',
    title: 'Upload your resume',
    description: 'Upload your resume and let our AI analyze it for optimization opportunities and ATS compatibility.',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M19.5 14.25v-2.625a3.375 3.375 0 0 0-3.375-3.375h-1.5A1.125 1.125 0 0 1 13.5 7.125v-1.5a3.375 3.375 0 0 0-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 0 0-9-9Z" />
      </svg>
    ),
  },
  {
    step: '03',
    title: 'Discover matching jobs',
    description: 'Browse AI-curated job recommendations based on your skills, experience, and preferences.',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="11" cy="11" r="8" />
        <path d="m21 21-4.35-4.35" />
      </svg>
    ),
  },
  {
    step: '04',
    title: 'Apply and improve',
    description: 'Apply to roles and use AI feedback to continuously strengthen your applications and interview skills.',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M13.5 6 5.25 12h13.5L10.5 18" />
      </svg>
    ),
  },
];

const recruiterSteps = [
  {
    step: '01',
    title: 'Create company profile',
    description: 'Set up your company page with culture, benefits, and growth information to attract the right candidates.',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 21v-8.25M15.75 21v-8.25M8.25 21v-8.25M3 9l9-6 9 6m-1.5 12V10.332A48.36 48.36 0 0 0 12 9.75c-2.551 0-5.056.2-7.5.582V21" />
      </svg>
    ),
  },
  {
    step: '02',
    title: 'Post job openings',
    description: 'Create detailed job postings with skill requirements. AI helps optimize your descriptions for better candidate attraction.',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 9v6m3-3H9m12 0a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
      </svg>
    ),
  },
  {
    step: '03',
    title: 'Discover candidates',
    description: 'AI surfaces the most relevant candidates for your roles. Review profiles, match scores, and skill assessments.',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M18 18.72a9.094 9.094 0 0 0 3.741-.479 3 3 0 0 0-4.682-2.72m.94 3.198.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0 1 12 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 0 1 6 18.719m12 0a5.971 5.971 0 0 0-.941-3.197m0 0A5.995 5.995 0 0 0 12 12.75a5.995 5.995 0 0 0-5.058 2.772m0 0a3 3 0 0 0-4.681 2.72 8.986 8.986 0 0 0 3.74.477m.94-3.197a5.971 5.971 0 0 0-.94 3.197M15 6.75a3 3 0 1 1-6 0 3 3 0 0 1 6 0Zm6 3a2.25 2.25 0 1 1-4.5 0 2.25 2.25 0 0 1 4.5 0Zm-13.5 0a2.25 2.25 0 1 1-4.5 0 2.25 2.25 0 0 1 4.5 0Z" />
      </svg>
    ),
  },
  {
    step: '04',
    title: 'Manage hiring',
    description: 'Track applications, schedule interviews, collaborate with your team, and make data-driven hiring decisions.',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M9 12.75 11.25 15 15 9.75m-3-7.036A11.959 11.959 0 0 1 3.598 6 11.99 11.99 0 0 0 3 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285Z" />
      </svg>
    ),
  },
];

export default function HowItWorks() {
  const [activeTab, setActiveTab] = useState<'candidate' | 'recruiter'>('candidate');
  const steps = activeTab === 'candidate' ? candidateSteps : recruiterSteps;

  return (
    <section className="py-16 sm:py-20 bg-neutral-50" id="how-it-works">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-2xl mx-auto mb-10">
          <p className="text-overline text-primary-600 mb-3">How It Works</p>
          <h2 className="text-display-sm text-neutral-900">
            Get started in minutes
          </h2>
          <p className="mt-3 text-body-lg text-neutral-500">
            Whether you're looking for your next role or building your team, JobRecruit makes the process straightforward.
          </p>
        </div>

        {/* Tab switcher */}
        <div className="flex justify-center mb-10">
          <div className="inline-flex rounded-lg bg-neutral-100 p-1">
            <button
              onClick={() => setActiveTab('candidate')}
              className={`px-4 py-2 text-sm font-medium rounded-md transition-colors ${
                activeTab === 'candidate'
                  ? 'bg-white text-neutral-900 shadow-sm'
                  : 'text-neutral-500 hover:text-neutral-700'
              }`}
            >
              For Candidates
            </button>
            <button
              onClick={() => setActiveTab('recruiter')}
              className={`px-4 py-2 text-sm font-medium rounded-md transition-colors ${
                activeTab === 'recruiter'
                  ? 'bg-white text-neutral-900 shadow-sm'
                  : 'text-neutral-500 hover:text-neutral-700'
              }`}
            >
              For Recruiters
            </button>
          </div>
        </div>

        {/* Steps */}
        <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {steps.map((item, index) => (
            <div key={item.step} className="relative">
              {/* Connector line */}
              {index < steps.length - 1 && (
                <div className="hidden lg:block absolute top-8 left-[calc(50%+2rem)] w-[calc(100%-4rem)] h-px bg-neutral-200" />
              )}
              <div className="relative bg-white rounded-xl p-5 ring-1 ring-neutral-200 hover:shadow-sm transition-shadow duration-200">
                <div className="flex items-center gap-3 mb-3">
                  <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary-50 text-primary-600">
                    {item.icon}
                  </div>
                  <span className="text-xs font-semibold text-primary-600 bg-primary-50 px-2 py-0.5 rounded-full">
                    Step {item.step}
                  </span>
                </div>
                <h3 className="text-heading-md text-neutral-900">{item.title}</h3>
                <p className="mt-1.5 text-body-sm text-neutral-500">{item.description}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
