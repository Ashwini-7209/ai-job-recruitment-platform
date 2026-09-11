import { Button } from '@/components/ui';
import PublicNavbar from '@/components/landing/PublicNavbar';
import PublicFooter from '@/components/landing/PublicFooter';

export default function NotFoundPage() {
  return (
    <div className="min-h-screen bg-white">
      <PublicNavbar />
      <main className="flex items-center justify-center py-20">
        <div className="text-center px-4">
          <p className="text-display-lg text-primary-600">404</p>
          <h1 className="mt-4 text-heading-lg text-neutral-900">
            Page not found
          </h1>
          <p className="mt-2 text-body-md text-neutral-600 max-w-md mx-auto">
            The page you're looking for doesn't exist or has been moved.
          </p>
          <div className="mt-6">
            <Button
              variant="primary"
              onClick={() => window.location.href = '/'}
            >
              Go back home
            </Button>
          </div>
        </div>
      </main>
      <PublicFooter />
    </div>
  );
}
