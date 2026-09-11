import {
  PublicNavbar,
  HeroSection,
  ValueSection,
  CandidateSection,
  RecruiterSection,
  AISection,
  HowItWorks,
  FeatureGrid,
  CTASection,
  PublicFooter,
} from '@/components/landing';

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-white">
      <PublicNavbar />
      <main>
        <HeroSection />
        <ValueSection />
        <CandidateSection />
        <RecruiterSection />
        <AISection />
        <HowItWorks />
        <FeatureGrid />
        <CTASection />
      </main>
      <PublicFooter />
    </div>
  );
}
