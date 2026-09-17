import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import { BarChart } from '@/components/ui/charts/BarChart';
import { Skeleton } from '@/components/ui/Skeleton';
import { analyticsService } from '@/services/analytics.service';
import type { AdminAnalyticsSummary, AdminJobAnalytics, AdminApplicationAnalytics, AdminInterviewAnalytics, TrendResponse } from '@/services/analytics.service';

export default function AdminAnalyticsPage() {
  const [summary, setSummary] = useState<AdminAnalyticsSummary | null>(null);
  const [jobAnalytics, setJobAnalytics] = useState<AdminJobAnalytics | null>(null);
  const [appAnalytics, setAppAnalytics] = useState<AdminApplicationAnalytics | null>(null);
  const [interviewAnalytics, setInterviewAnalytics] = useState<AdminInterviewAnalytics | null>(null);
  const [userGrowth, setUserGrowth] = useState<TrendResponse | null>(null);
  const [appTrend, setAppTrend] = useState<TrendResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [from, setFrom] = useState(() => {
    const d = new Date();
    d.setMonth(d.getMonth() - 6);
    return d.toISOString().split('T')[0];
  });
  const [to, setTo] = useState(() => new Date().toISOString().split('T')[0]);

  useEffect(() => {
    loadData();
  }, [from, to]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [summaryRes, jobRes, appRes, interviewRes, userGrowthRes, appTrendRes] = await Promise.all([
        analyticsService.getAdminSummary(),
        analyticsService.getAdminJobAnalytics(),
        analyticsService.getAdminApplicationAnalytics(),
        analyticsService.getAdminInterviewAnalytics(),
        analyticsService.getAdminUserGrowth(from, to),
        analyticsService.getAdminApplicationTrend(from, to),
      ]);
      setSummary(summaryRes.data ?? null);
      setJobAnalytics(jobRes.data ?? null);
      setAppAnalytics(appRes.data ?? null);
      setInterviewAnalytics(interviewRes.data ?? null);
      setUserGrowth(userGrowthRes.data ?? null);
      setAppTrend(appTrendRes.data ?? null);
    } catch (error) {
      console.error('Failed to load analytics:', error);
    } finally {
      setLoading(false);
    }
  };

  const summaryCards = summary ? [
    { title: 'Total Users', value: summary.totalUsers },
    { title: 'Candidates', value: summary.totalCandidates },
    { title: 'Recruiters', value: summary.totalRecruiters },
    { title: 'Active Users', value: summary.activeUsers },
    { title: 'Total Jobs', value: summary.totalJobs },
    { title: 'Published Jobs', value: summary.publishedJobs },
    { title: 'Total Applications', value: summary.totalApplications },
    { title: 'Total Interviews', value: summary.totalInterviews },
  ] : [];

  const jobChartData = jobAnalytics ? [
    { label: 'Draft', value: jobAnalytics.draftJobs },
    { label: 'Published', value: jobAnalytics.publishedJobs },
    { label: 'Closed', value: jobAnalytics.closedJobs },
  ] : [];

  const appChartData = appAnalytics
    ? Object.entries(appAnalytics.statusDistribution).map(([key, value]) => ({
        label: key.charAt(0) + key.slice(1).toLowerCase().replace('_', ' '),
        value,
      }))
    : [];

  const interviewChartData = interviewAnalytics
    ? Object.entries(interviewAnalytics.statusDistribution).map(([key, value]) => ({
        label: key.charAt(0) + key.slice(1).toLowerCase().replace('_', ' '),
        value,
      }))
    : [];

  const userGrowthChartData = userGrowth
    ? userGrowth.data.map((d) => ({ label: d.date, value: d.count }))
    : [];

  const appTrendChartData = appTrend
    ? appTrend.data.map((d) => ({ label: d.date, value: d.count }))
    : [];

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-[var(--content-max-width)] mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-heading-lg text-neutral-900">Platform Analytics</h1>
          <p className="text-body-md text-neutral-500">Overview of platform activity</p>
        </div>
        <div className="flex gap-2">
          <input
            type="date"
            value={from}
            onChange={(e) => setFrom(e.target.value)}
            className="border border-neutral-200 rounded-lg px-3 py-1.5 text-body-sm focus:outline-none focus:ring-2 focus:ring-secondary-500"
          />
          <input
            type="date"
            value={to}
            onChange={(e) => setTo(e.target.value)}
            className="border border-neutral-200 rounded-lg px-3 py-1.5 text-body-sm focus:outline-none focus:ring-2 focus:ring-secondary-500"
          />
        </div>
      </div>

      {loading ? (
        <div className="grid grid-cols-2 sm:grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
          {[...Array(8)].map((_, i) => (
            <Skeleton key={i} className="h-24" />
          ))}
        </div>
      ) : (
        <>
          <div className="grid grid-cols-2 sm:grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
            {summaryCards.map((card) => (
              <Card key={card.title}>
                <CardContent className="pt-6">
                  <p className="text-body-sm text-neutral-500">{card.title}</p>
                  <p className="text-heading-md text-neutral-900">{card.value}</p>
                </CardContent>
              </Card>
            ))}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-6">
            <Card>
              <CardHeader>
                <CardTitle>User Growth</CardTitle>
              </CardHeader>
              <CardContent>
                {userGrowthChartData.length > 0 ? (
                  <BarChart data={userGrowthChartData} height={200} />
                ) : (
                  <p className="text-neutral-500 text-center py-8">No data available</p>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Job Status Distribution</CardTitle>
              </CardHeader>
              <CardContent>
                {jobChartData.length > 0 ? (
                  <BarChart data={jobChartData} height={200} />
                ) : (
                  <p className="text-neutral-500 text-center py-8">No data available</p>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Application Status</CardTitle>
              </CardHeader>
              <CardContent>
                {appChartData.length > 0 ? (
                  <BarChart data={appChartData} height={200} />
                ) : (
                  <p className="text-neutral-500 text-center py-8">No data available</p>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Interview Status</CardTitle>
              </CardHeader>
              <CardContent>
                {interviewChartData.length > 0 ? (
                  <BarChart data={interviewChartData} height={200} />
                ) : (
                  <p className="text-neutral-500 text-center py-8">No data available</p>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Application Trend</CardTitle>
              </CardHeader>
              <CardContent>
                {appTrendChartData.length > 0 ? (
                  <BarChart data={appTrendChartData} height={200} />
                ) : (
                  <p className="text-neutral-500 text-center py-8">No data available</p>
                )}
              </CardContent>
            </Card>
          </div>
        </>
      )}
    </div>
  );
}
