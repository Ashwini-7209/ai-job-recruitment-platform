import { Card, CardContent } from '@/components/ui';

const values = [
  {
    icon: (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M9.813 15.904 9 18.75l-.813-2.846a4.5 4.5 0 0 0-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 0 0 3.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 0 0 3.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 0 0-3.09 3.09ZM18.259 8.715 18 9.75l-.259-1.035a3.375 3.375 0 0 0-2.455-2.456L14.25 6l1.036-.259a3.375 3.375 0 0 0 2.455-2.456L18 2.25l.259 1.035a3.375 3.375 0 0 0 2.455 2.456L21.75 6l-1.036.259a3.375 3.375 0 0 0-2.455 2.456ZM16.894 20.567 16.5 21.75l-.394-1.183a2.25 2.25 0 0 0-1.423-1.423L13.5 18.75l1.183-.394a2.25 2.25 0 0 0 1.423-1.423l.394-1.183.394 1.183a2.25 2.25 0 0 0 1.423 1.423l1.183.394-1.183.394a2.25 2.25 0 0 0-1.423 1.423Z" />
      </svg>
    ),
    title: 'AI Career Intelligence',
    description: 'Get data-driven insights into your career trajectory. Our AI analyzes market trends, skill demands, and compensation data to guide your next move.',
  },
  {
    icon: (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M7.5 21 3 16.5m0 0L7.5 12M3 16.5h13.5m0-13.5L21 7.5m0 0L16.5 12M21 7.5H7.5" />
      </svg>
    ),
    title: 'Smarter Matching',
    description: 'Our algorithms match candidates to roles based on skills, experience, and cultural fit—not just keyword scraping. Every recommendation is contextual.',
  },
  {
    icon: (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M3.75 12h16.5m-16.5 3.75h16.5M3.75 19.5h16.5M5.625 4.5h12.75a1.875 1.875 0 0 1 0 3.75H5.625a1.875 1.875 0 0 1 0-3.75Z" />
      </svg>
    ),
    title: 'Streamlined Recruitment',
    description: 'Automate screening, shortlisting, and scheduling. Recruiters spend less time on admin and more time building relationships with top talent.',
  },
  {
    icon: (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M9 12.75 11.25 15 15 9.75m-3-7.036A11.959 11.959 0 0 1 3.598 6 11.99 11.99 0 0 0 3 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285Z" />
      </svg>
    ),
    title: 'Better Hiring Decisions',
    description: 'Reduce bad hires with structured assessments, skill verification, and AI-powered candidate scoring that goes beyond the resume.',
  },
];

export default function ValueSection() {
  return (
    <section className="py-16 sm:py-20 bg-white" id="value">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-2xl mx-auto mb-12">
          <p className="text-overline text-secondary-500 mb-3">Why HireFlow</p>
          <h2 className="text-display-sm text-primary-900">
            Recruitment built for the modern workforce
          </h2>
          <p className="mt-3 text-body-lg text-neutral-500">
            We combine artificial intelligence with human-centered design to create
            a recruitment experience that works for everyone.
          </p>
        </div>
        <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-5">
          {values.map((item) => (
            <Card key={item.title} padding="lg" className="group hover:shadow-md transition-shadow duration-200">
              <CardContent>
                <div className="flex h-11 w-11 items-center justify-center rounded-lg bg-secondary-50 text-secondary-700 mb-4 group-hover:bg-secondary-100 transition-colors">
                  {item.icon}
                </div>
                <h3 className="text-heading-md text-primary-900">{item.title}</h3>
                <p className="mt-2 text-body-sm text-neutral-500 leading-relaxed">{item.description}</p>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    </section>
  );
}
