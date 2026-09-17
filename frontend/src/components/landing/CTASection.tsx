import { Link } from 'react-router-dom';
import { Button } from '@/components/ui';

export default function CTASection() {
  return (
    <section className="py-16 sm:py-20 bg-gradient-to-br from-primary-900 via-primary-800 to-primary-950">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="max-w-3xl mx-auto text-center">
          <h2 className="text-display-sm text-white">
            Build your next career move with smarter recruitment
          </h2>
          <p className="mt-4 text-body-lg text-neutral-300">
            Whether you're a candidate seeking the right role or a recruiter building your team,
            HireFlow provides the intelligence and tools to make it happen.
          </p>
          <div className="mt-6 sm:mt-8 flex flex-col sm:flex-row gap-3 justify-center">
            <Link to="/register/candidate" className="w-full sm:w-auto">
              <Button size="lg" variant="secondary" className="bg-white text-primary-800 hover:bg-neutral-100 w-full sm:w-auto">
                Find Your Next Role
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none" className="ml-1">
                  <path d="M3 8h10M9 4l4 4-4 4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              </Button>
            </Link>
            <Link to="/register/recruiter" className="w-full sm:w-auto">
              <Button size="lg" variant="outline" className="border-white/30 text-white hover:bg-white/10 w-full sm:w-auto">
                Start Hiring Today
              </Button>
            </Link>
          </div>
          <p className="mt-6 text-sm text-neutral-400">
            Free to get started. No credit card required.
          </p>
        </div>
      </div>
    </section>
  );
}
