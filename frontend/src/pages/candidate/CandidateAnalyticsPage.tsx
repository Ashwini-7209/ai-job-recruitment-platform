import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import { BarChart } from '@/components/ui/charts/BarChart';
import { Skeleton } from '@/components/ui/Skeleton';
import { analyticsService } from '@/services/analytics.service';
import type { CandidateAnalyticsSummary, ApplicationStatusDistribution, TrendResponse } from '@/services/analytics.service';

export default function CandidateAnalyticsPage() {
  const [summary, setSummary] = useState<CandidateAnalyticsSummary | null>(null);
  const [statusDist, setStatusDist] = useState<ApplicationStatusDistribution | null>(null);
  const [trend, setTrend] = useState<TrendResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [from, setFrom] = useState(() => {
    const d = new Date();
    d.setMonth(d.getMonth() - 3);
    return d.toISOString().split('T')[0];
  });
  const [to, setTo] = useState(() => new Date().toISOString().split('T')[0]);

  useEffect(() => {
    loadData();
  }, [from, to]);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [summaryRes, statusRes, trendRes] = await Promise.all([
        analyticsService.getCandidateSummary(),
        analyticsService.getCandidateApplicationStatus(),
        analyticsService.getCandidateApplicationTrend(from, to),
      ]);
      setSummary(summaryRes.data ?? null);
      setStatusDist(statusRes.data ?? null);
      setTrend(trendRes.data ?? null);
    } catch (error) {
      console.error('Failed to load analytics:', error);
      setError('Failed to load analytics data. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const hasData = summary && summary.totalApplications > 0;

  const summaryCards = summary ? [
    { title: 'Total Applications', value: summary.totalApplications },
    { title: 'Under Review', value: summary.applicationsUnderReview },
    { title: 'Shortlisted', value: summary.applicationsShortlisted },
    { title: 'Hired', value: summary.applicationsHired },
    { title: 'Rejected', value: summary.applicationsRejected },
    { title: 'Upcoming Interviews', value: summary.upcomingInterviews },
    { title: 'Saved Jobs', value: summary.totalSavedJobs },
    { title: 'Active Alerts', value: summary.activeJobAlerts },
  ] : [];

  const statusChartData = statusDist?.statusDistribution
    ? Object.entries(statusDist.statusDistribution).map(([key, value]) => ({
        label: key.charAt(0) + key.slice(1).toLowerCase().replace('_', ' '),
        value,
      }))
    : [];

  const trendChartData = trend?.data
    ? trend.data.map((d) => ({
        label: d.date,
        value: d.count,
      }))
    : [];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">My Analytics</h1>
          <p className="text-gray-500">Track your job search progress</p>
        </div>
        <div className="flex gap-2">
          <input
            type="date"
            value={from}
            onChange={(e) => setFrom(e.target.value)}
            className="border rounded px-3 py-1 text-sm"
          />
          <input
            type="date"
            value={to}
            onChange={(e) => setTo(e.target.value)}
            className="border rounded px-3 py-1 text-sm"
          />
        </div>
      </div>

      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {[...Array(8)].map((_, i) => (
            <Skeleton key={i} className="h-24" />
          ))}
        </div>
      ) : error ? (
        <Card>
          <CardContent className="py-12">
            <p className="text-red-500 text-center">{error}</p>
          </CardContent>
        </Card>
      ) : !hasData ? (
        <Card>
          <CardContent className="py-12">
            <p className="text-gray-500 text-center">No analytics data available yet. Start applying to jobs to see your analytics here.</p>
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            {summaryCards.map((card) => (
              <Card key={card.title}>
                <CardContent className="pt-6">
                  <p className="text-sm text-gray-500">{card.title}</p>
                  <p className="text-2xl font-bold">{card.value}</p>
                </CardContent>
              </Card>
            ))}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <CardTitle>Application Status</CardTitle>
              </CardHeader>
              <CardContent>
                {statusChartData.length > 0 ? (
                  <BarChart data={statusChartData} height={200} />
                ) : (
                  <p className="text-gray-500 text-center py-8">No data available</p>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Application Trend</CardTitle>
              </CardHeader>
              <CardContent>
                {trendChartData.length > 0 ? (
                  <BarChart data={trendChartData} height={200} />
                ) : (
                  <p className="text-gray-500 text-center py-8">No data available</p>
                )}
              </CardContent>
            </Card>
          </div>
        </>
      )}
    </div>
  );
}
