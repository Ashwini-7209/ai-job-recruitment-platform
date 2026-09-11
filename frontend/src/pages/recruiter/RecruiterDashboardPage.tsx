import { useState, useEffect } from 'react';
import { Card, Badge, Button, Skeleton } from '@/components/ui';
import { analyticsService } from '@/services/analytics.service';
import type { RecruiterAnalyticsSummary } from '@/services/analytics.service';
import {
  activeJobs,
  candidatePipeline,
  recentApplications,
  upcomingInterviews,
  recruiterInsights,
  quickActions,
  companyInfo,
} from '@/data/recruiter';
import type { RecruiterJob, RecruiterCandidate, RecruiterApplication, RecruiterInterview } from '@/types/recruiter';

const jobStatusColors: Record<string, 'success' | 'warning' | 'error' | 'default'> = {
  Active: 'success',
  Paused: 'warning',
  Closed: 'error',
  Draft: 'default',
};

const applicationStatusColors: Record<string, 'info' | 'warning' | 'success' | 'error' | 'primary' | 'default' | 'accent'> = {
  New: 'info',
  Screening: 'warning',
  Shortlisted: 'accent',
  Interview: 'primary',
  Rejected: 'error',
  Hired: 'success',
};

const stageColors: Record<string, 'info' | 'warning' | 'success' | 'primary' | 'accent' | 'default'> = {
  New: 'info',
  Screening: 'warning',
  Shortlisted: 'accent',
  Interview: 'primary',
  Selected: 'success',
};

const insightTypeIcons: Record<string, string> = {
  matching: 'M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z',
  description: 'M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z',
  pipeline: 'M3.75 12h16.5m-16.5 3.75h16.5M3.75 19.5h16.5M5.625 4.5h12.75a1.875 1.875 0 010 3.75H5.625a1.875 1.875 0 010-3.75z',
  bottleneck: 'M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z',
};

function HiringOverviewSection({ summary, loading }: { summary: RecruiterAnalyticsSummary | null; loading: boolean }) {
  const metrics = summary ? [
    { label: 'Active Jobs', value: summary.publishedJobs, icon: 'M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z' },
    { label: 'Total Applications', value: summary.totalApplications, icon: 'M9 12h3.75M9 15h3.75M9 18h3.75m3 .75H18a2.25 2.25 0 002.25-2.25V6.108c0-1.135-.845-2.098-1.976-2.192a48.424 48.424 0 00-1.123-.08m-5.801 0c-.065.21-.1.433-.1.664 0 .414.336.75.75.75h4.5a.75.75 0 00.75-.75 2.25 2.25 0 00-.1-.664m-5.8 0A2.251 2.251 0 0113.5 2.25H15c1.012 0 1.867.668 2.15 1.586m-5.8 0c-.376.023-.75.05-1.124.08C9.095 4.01 8.25 4.973 8.25 6.108V8.25' },
    { label: 'Shortlisted', value: summary.shortlistedCandidates, icon: 'M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z' },
    { label: 'Hired', value: summary.hiredCandidates, icon: 'M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z' },
    { label: 'Interviews', value: summary.upcomingInterviews, icon: 'M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 012.25-2.25h13.5A2.25 2.25 0 0121 7.5v11.25m-18 0A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75m-18 0v-7.5A2.25 2.25 0 015.25 9h13.5A2.25 2.25 0 0121 11.25v7.5' },
  ] : [
    { label: 'Active Jobs', value: 0, icon: '' },
    { label: 'Total Applications', value: 0, icon: '' },
    { label: 'Shortlisted', value: 0, icon: '' },
    { label: 'Hired', value: 0, icon: '' },
    { label: 'Interviews', value: 0, icon: '' },
  ];

  return (
    <section>
      <h2 className="text-heading-md text-neutral-900 mb-4">Hiring Overview</h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {metrics.slice(0, 4).map((metric) => (
          <Card key={metric.label} padding="md">
            <div className="flex items-start justify-between">
              <div>
                <p className="text-body-sm text-neutral-500">{metric.label}</p>
                {loading ? (
                  <Skeleton className="h-8 w-16 mt-1" />
                ) : (
                  <p className="text-display-sm text-neutral-900 mt-1">{metric.value}</p>
                )}
              </div>
            </div>
          </Card>
        ))}
      </div>
    </section>
  );
}

function HiringFunnelSection() {
  const [funnel, setFunnel] = useState<Record<string, number>>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const res = await analyticsService.getRecruiterFunnel();
        setFunnel(res.data?.funnel ?? {});
      } catch {
        // fallback
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const stages = [
    { label: 'APPLIED', color: 'bg-info-500' },
    { label: 'UNDER_REVIEW', color: 'bg-warning-500' },
    { label: 'SHORTLISTED', color: 'bg-success-500' },
    { label: 'HIRED', color: 'bg-primary-500' },
    { label: 'REJECTED', color: 'bg-error-500' },
  ];

  const maxCount = Math.max(...stages.map((s) => funnel[s.label] || 0), 1);

  return (
    <section>
      <h2 className="text-heading-md text-neutral-900 mb-4">Hiring Funnel</h2>
      <Card padding="md">
        {loading ? (
          <div className="space-y-3">
            {[...Array(5)].map((_, i) => <Skeleton key={i} className="h-8 w-full" />)}
          </div>
        ) : (
          <div className="space-y-3">
            {stages.map((stage) => {
              const count = funnel[stage.label] || 0;
              return (
                <div key={stage.label} className="flex items-center gap-4">
                  <span className="text-body-sm text-neutral-600 w-24 shrink-0">{stage.label.replace('_', ' ').toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase())}</span>
                  <div className="flex-1 h-8 bg-neutral-100 rounded-lg overflow-hidden">
                    <div
                      className={`h-full ${stage.color} rounded-lg transition-all duration-500`}
                      style={{ width: `${(count / maxCount) * 100}%` }}
                    />
                  </div>
                  <span className="text-body-sm font-medium text-neutral-900 w-12 text-right">{count}</span>
                </div>
              );
            })}
          </div>
        )}
      </Card>
    </section>
  );
}

function ActiveJobsSection() {
  return (
    <section>
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-heading-md text-neutral-900">Active Jobs</h2>
        <Button variant="ghost" size="sm">View All</Button>
      </div>
      <div className="space-y-3">
        {activeJobs.map((job: RecruiterJob) => (
          <Card key={job.id} padding="md">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div className="min-w-0 flex-1">
                <div className="flex items-center gap-2 flex-wrap">
                  <h3 className="text-body-md font-semibold text-neutral-900">{job.title}</h3>
                  <Badge variant={jobStatusColors[job.status]} size="sm" dot>{job.status}</Badge>
                </div>
                <div className="flex items-center gap-3 mt-1 text-body-sm text-neutral-500 flex-wrap">
                  <span>{job.department}</span>
                  <span className="hidden sm:inline">·</span>
                  <span>{job.location}</span>
                  <span className="hidden sm:inline">·</span>
                  <span>{job.type}</span>
                </div>
              </div>
              <div className="flex items-center gap-4">
                <div className="text-center">
                  <p className="text-body-md font-semibold text-neutral-900">{job.applications}</p>
                  <p className="text-body-sm text-neutral-500">apps</p>
                </div>
                <span className="text-body-sm text-neutral-400">{job.postedDate}</span>
              </div>
            </div>
          </Card>
        ))}
      </div>
    </section>
  );
}

function CandidatePipelineSection() {
  const stages = ['New', 'Screening', 'Shortlisted', 'Interview', 'Selected'] as const;

  return (
    <section>
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-heading-md text-neutral-900">Candidate Pipeline</h2>
        <Button variant="ghost" size="sm">View All</Button>
      </div>
      <div className="overflow-x-auto -mx-4 px-4 sm:mx-0 sm:px-0">
        <div className="flex gap-3 min-w-[800px] sm:min-w-0">
          {stages.map((stage) => {
            const stageCandidates = candidatePipeline.filter((c) => c.stage === stage);
            return (
              <div key={stage} className="flex-1 min-w-[160px]">
                <div className="flex items-center gap-2 mb-3">
                  <Badge variant={stageColors[stage]} size="sm">{stage}</Badge>
                  <span className="text-body-sm text-neutral-500">{stageCandidates.length}</span>
                </div>
                <div className="space-y-2">
                  {stageCandidates.map((candidate: RecruiterCandidate) => (
                    <div key={candidate.id} className="rounded-lg border border-neutral-200 bg-white p-3 hover:shadow-sm transition-shadow">
                      <div className="flex items-center justify-between">
                        <p className="text-body-sm font-medium text-neutral-900 truncate">{candidate.name}</p>
                        <span className={`text-xs font-medium ${candidate.matchScore >= 85 ? 'text-success-600' : candidate.matchScore >= 70 ? 'text-primary-600' : 'text-neutral-500'}`}>
                          {candidate.matchScore}%
                        </span>
                      </div>
                      <p className="text-body-sm text-neutral-500 mt-0.5">{candidate.role}</p>
                      <p className="text-body-sm text-neutral-400">{candidate.experience}</p>
                      <div className="flex flex-wrap gap-1 mt-2">
                        {candidate.skills.slice(0, 2).map((skill) => (
                          <span key={skill} className="text-xs bg-neutral-100 text-neutral-600 px-1.5 py-0.5 rounded">
                            {skill}
                          </span>
                        ))}
                        {candidate.skills.length > 2 && (
                          <span className="text-xs text-neutral-400">+{candidate.skills.length - 2}</span>
                        )}
                      </div>
                    </div>
                  ))}
                  {stageCandidates.length === 0 && (
                    <p className="text-body-sm text-neutral-400 text-center py-4">No candidates</p>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}

function RecentApplicationsSection() {
  return (
    <section>
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-heading-md text-neutral-900">Recent Applications</h2>
        <Button variant="ghost" size="sm">View All</Button>
      </div>
      {/* Desktop table */}
      <div className="hidden md:block">
        <Card padding="none">
          <div className="overflow-x-auto">
            <table className="w-full text-left">
              <thead>
                <tr className="border-b border-neutral-100">
                  <th className="px-4 py-3 text-overline">Candidate</th>
                  <th className="px-4 py-3 text-overline">Job</th>
                  <th className="px-4 py-3 text-overline">Applied</th>
                  <th className="px-4 py-3 text-overline">Status</th>
                  <th className="px-4 py-3 text-overline">Match</th>
                  <th className="px-4 py-3 text-overline text-right">Action</th>
                </tr>
              </thead>
              <tbody>
                {recentApplications.map((app: RecruiterApplication) => (
                  <tr key={app.id} className="border-b border-neutral-50 last:border-0 hover:bg-neutral-50/50 transition-colors">
                    <td className="px-4 py-3">
                      <span className="text-body-sm font-medium text-neutral-900">{app.candidateName}</span>
                    </td>
                    <td className="px-4 py-3">
                      <span className="text-body-sm text-neutral-600">{app.jobTitle}</span>
                    </td>
                    <td className="px-4 py-3">
                      <span className="text-body-sm text-neutral-500">{app.appliedDate}</span>
                    </td>
                    <td className="px-4 py-3">
                      <Badge variant={applicationStatusColors[app.status]} size="sm">{app.status}</Badge>
                    </td>
                    <td className="px-4 py-3">
                      <span className={`text-body-sm font-medium ${app.matchScore >= 85 ? 'text-success-600' : app.matchScore >= 70 ? 'text-primary-600' : 'text-neutral-500'}`}>
                        {app.matchScore}%
                      </span>
                    </td>
                    <td className="px-4 py-3 text-right">
                      <Button variant="ghost" size="sm">Review</Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      </div>
      {/* Mobile cards */}
      <div className="md:hidden space-y-3">
        {recentApplications.map((app: RecruiterApplication) => (
          <Card key={app.id} padding="md">
            <div className="flex items-start justify-between">
              <div>
                <p className="text-body-md font-medium text-neutral-900">{app.candidateName}</p>
                <p className="text-body-sm text-neutral-500 mt-0.5">{app.jobTitle}</p>
              </div>
              <Badge variant={applicationStatusColors[app.status]} size="sm">{app.status}</Badge>
            </div>
            <div className="flex items-center justify-between mt-3">
              <span className="text-body-sm text-neutral-500">{app.appliedDate}</span>
              <span className={`text-body-sm font-medium ${app.matchScore >= 85 ? 'text-success-600' : app.matchScore >= 70 ? 'text-primary-600' : 'text-neutral-500'}`}>
                {app.matchScore}% match
              </span>
            </div>
          </Card>
        ))}
      </div>
    </section>
  );
}

function UpcomingInterviewsSection() {
  return (
    <section>
      <h2 className="text-heading-md text-neutral-900 mb-4">Upcoming Interviews</h2>
      <div className="space-y-3">
        {upcomingInterviews.map((interview: RecruiterInterview) => (
          <Card key={interview.id} padding="md">
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0 flex-1">
                <p className="text-body-md font-medium text-neutral-900">{interview.candidateName}</p>
                <p className="text-body-sm text-neutral-500 mt-0.5">{interview.jobTitle}</p>
                <div className="flex items-center gap-2 mt-2 text-body-sm text-neutral-500">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
                    <line x1="16" y1="2" x2="16" y2="6" />
                    <line x1="8" y1="2" x2="8" y2="6" />
                    <line x1="3" y1="10" x2="21" y2="10" />
                  </svg>
                  <span>{interview.date} at {interview.time}</span>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <Badge variant={interview.type === 'Video' ? 'info' : interview.type === 'Phone' ? 'warning' : 'accent'} size="sm">
                  {interview.type}
                </Badge>
              </div>
            </div>
          </Card>
        ))}
      </div>
    </section>
  );
}

function AIInsightsSection() {
  return (
    <section>
      <div className="flex items-center gap-2 mb-4">
        <div className="flex h-6 w-6 items-center justify-center rounded-md bg-accent-100">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="text-accent-600" strokeLinecap="round" strokeLinejoin="round">
            <polygon points="13,2 3,14 12,14 11,22 21,10 12,10" />
          </svg>
        </div>
        <h2 className="text-heading-md text-neutral-900">AI Recruitment Insights</h2>
      </div>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        {recruiterInsights.map((insight) => (
          <Card key={insight.id} padding="md" className="hover:shadow-sm transition-shadow">
            <div className="flex items-start gap-3">
              <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-accent-50">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="text-accent-600" strokeLinecap="round" strokeLinejoin="round">
                  <path d={insightTypeIcons[insight.type]} />
                </svg>
              </div>
              <div className="min-w-0 flex-1">
                <h3 className="text-body-md font-semibold text-neutral-900">{insight.title}</h3>
                <p className="text-body-sm text-neutral-500 mt-1">{insight.description}</p>
                <Button variant="ghost" size="sm" className="mt-2 -ml-2">
                  {insight.actionLabel}
                </Button>
              </div>
            </div>
          </Card>
        ))}
      </div>
    </section>
  );
}

function QuickActionsSection() {
  return (
    <section>
      <h2 className="text-heading-md text-neutral-900 mb-4">Quick Actions</h2>
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        {quickActions.map((action) => (
          <Card key={action.label} padding="md" className="hover:shadow-sm transition-shadow cursor-pointer">
            <div className="flex flex-col items-center text-center gap-2">
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary-50">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="text-primary-600" strokeLinecap="round" strokeLinejoin="round">
                  <path d={action.icon} />
                </svg>
              </div>
              <span className="text-body-sm font-medium text-neutral-700">{action.label}</span>
            </div>
          </Card>
        ))}
      </div>
    </section>
  );
}

function CompanyIdentitySection() {
  return (
    <section>
      <h2 className="text-heading-md text-neutral-900 mb-4">Company</h2>
      <Card padding="md">
        <div className="flex items-center gap-4">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-secondary-100 text-secondary-700 font-semibold text-lg">
            {companyInfo.initials}
          </div>
          <div>
            <p className="text-body-md font-semibold text-neutral-900">{companyInfo.name}</p>
            <p className="text-body-sm text-neutral-500">{companyInfo.role}</p>
          </div>
        </div>
      </Card>
    </section>
  );
}

function getGreeting(): string {
  const hour = new Date().getHours();
  if (hour < 12) return 'Good morning';
  if (hour < 18) return 'Good afternoon';
  return 'Good evening';
}

export default function RecruiterDashboardPage() {
  const [summary, setSummary] = useState<RecruiterAnalyticsSummary | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const res = await analyticsService.getRecruiterSummary();
        setSummary(res.data ?? null);
      } catch {
        // fallback to mock
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-8 max-w-7xl mx-auto">
      {/* Welcome */}
      <div>
        <h1 className="text-heading-lg text-neutral-900">{getGreeting()}, Recruiter</h1>
        <p className="text-body-md text-neutral-500 mt-1">Here's what's happening with your hiring pipeline today.</p>
      </div>

      <HiringOverviewSection summary={summary} loading={loading} />

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <HiringFunnelSection />
        <CompanyIdentitySection />
      </div>

      <ActiveJobsSection />

      <CandidatePipelineSection />

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <RecentApplicationsSection />
        <UpcomingInterviewsSection />
      </div>

      <AIInsightsSection />

      <QuickActionsSection />
    </div>
  );
}
