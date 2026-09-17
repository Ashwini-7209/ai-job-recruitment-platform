const footerLinks = {
  platform: [
    { label: 'Features', href: '#features' },
    { label: 'Companies', href: '#' },
    { label: 'Salary Explorer', href: '#' },
    { label: 'Skill Assessments', href: '#' },
  ],
  candidates: [
    { label: 'Create Profile', href: '#' },
    { label: 'Resume Builder', href: '#' },
    { label: 'AI Career Coach', href: '#' },
    { label: 'Interview Prep', href: '#' },
  ],
  recruiters: [
    { label: 'Post a Job', href: '#' },
    { label: 'Candidate Search', href: '#' },
    { label: 'ATS Integration', href: '#' },
    { label: 'Hiring Analytics', href: '#' },
  ],
  aiFeatures: [
    { label: 'Resume Analysis', href: '#' },
    { label: 'Job Matching', href: '#' },
    { label: 'Skill Gap Analysis', href: '#' },
    { label: 'Interview Feedback', href: '#' },
  ],
  company: [
    { label: 'About Us', href: '#' },
    { label: 'Careers', href: '#' },
    { label: 'Blog', href: '#' },
    { label: 'Contact', href: '#' },
  ],
};

export default function PublicFooter() {
  return (
    <footer className="bg-primary-900 text-neutral-400">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 pt-12 pb-8">
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-8">
          {/* Brand */}
          <div className="col-span-2 md:col-span-3 lg:col-span-1 mb-4 lg:mb-0">
            <a href="/" className="flex items-center gap-2.5 mb-4" aria-label="JobRecruit Home">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-white/10">
                <svg className="h-4.5 w-4.5 text-white" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M20.25 14.15v4.25c0 1.094-.787 2.036-1.872 2.18-2.087.277-4.216.42-6.378.42s-4.291-.143-6.378-.42c-1.085-.144-1.872-1.086-1.872-2.18v-4.25m16.5 0a2.18 2.18 0 0 0 .75-1.661V8.706c0-1.081-.768-2.015-1.837-2.175a48.114 48.114 0 0 0-3.413-.387m4.5 8.006c-.194.165-.42.295-.673.38A23.978 23.978 0 0 1 12 15.75c-2.648 0-5.195-.429-7.577-1.22a2.016 2.016 0 0 1-.673-.38m0 0A2.18 2.18 0 0 1 3 12.489V8.706c0-1.081.768-2.015 1.837-2.175a48.111 48.111 0 0 1 3.413-.387m7.5 0V5.25A2.25 2.25 0 0 0 13.5 3h-3a2.25 2.25 0 0 0-2.25 2.25v.894m7.5 0a48.667 48.667 0 0 0-7.5 0" />
                </svg>
              </div>
              <span className="text-lg font-bold text-white">HireFlow</span>
            </a>
            <p className="text-sm text-neutral-400 max-w-xs">
              AI-powered recruitment platform connecting talented candidates with the right opportunities.
            </p>
          </div>

          {/* Link columns */}
          {Object.entries(footerLinks).map(([category, links]) => (
            <div key={category}>
              <h3 className="text-label-lg text-neutral-200 mb-3 capitalize">
                {category === 'aiFeatures' ? 'AI Features' : category}
              </h3>
              <ul className="space-y-2">
                {links.map((link) => (
                  <li key={link.label}>
                    <a
                      href={link.href}
                      className="text-sm text-neutral-400 hover:text-white transition-colors duration-150"
                    >
                      {link.label}
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        {/* Bottom bar */}
        <div className="mt-10 pt-6 border-t border-white/10 flex flex-col sm:flex-row items-center justify-between gap-4">
          <p className="text-sm text-neutral-400">
            &copy; {new Date().getFullYear()} HireFlow. All rights reserved.
          </p>
          <div className="flex items-center gap-4 text-sm text-neutral-400">
            <a href="#" className="hover:text-white transition-colors">Privacy Policy</a>
            <a href="#" className="hover:text-white transition-colors">Terms of Service</a>
            <a href="#" className="hover:text-white transition-colors">Cookie Policy</a>
          </div>
        </div>
      </div>
    </footer>
  );
}
