import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import { BarChart } from '@/components/ui/charts/BarChart';
import { Skeleton } from '@/components/ui/Skeleton';
import { analyticsService } from '@/services/analytics.service';
import type { RecruiterAnalyticsSummary, RecruiterFunnel, RecruiterHiringAnalytics, TrendResponse } from '@/services/analytics.service';

export default function RecruiterAnalyticsPage() {
  const [summary, setSummary] = useState<RecruiterAnalyticsSummary | null>(null);
  const [funnel, setFunnel] = useState<RecruiterFunnel | null>(null);
  const [hiringMetrics, setHiringMetrics] = useState<RecruiterHiringAnalytics | null>(null);
  const [trend, setTrend] = useState<TrendResponse | null>(null);
  const [loading, setLoading] = useState(true);
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
    try {
      const [summaryRes, funnelRes, hiringRes, trendRes] = await Promise.all([
        analyticsService.getRecruiterSummary(),
        analyticsService.getRecruiterFunnel(),
        analyticsService.getRecruiterHiringMetrics(),
        analyticsService.getRecruiterApplicationTrend(from, to),
      ]);
      setSummary(summaryRes.data ?? null);
      setFunnel(funnelRes.data ?? null);
      setHiringMetrics(hiringRes.data ?? null);
      setTrend(trendRes.data ?? null);
    } catch (error) {
      console.error('Failed to load analytics:', error);
    } finally {
      setLoading(false);
    }
  };

  const summaryCards = summary ? [
    { title: 'Total Jobs', value: summary.totalJobs },
    { title: 'Published Jobs', value: summary.publishedJobs },
    { title: 'Total Applications', value: summary.totalApplications },
    { title: 'Under Review', value: summary.applicationsUnderReview },
    { title: 'Shortlisted', value: summary.shortlistedCandidates },
    { title: 'Hired', value: summary.hiredCandidates },
    { title: 'Upcoming Interviews', value: summary.upcomingInterviews },
    { title: 'Completed Interviews', value: summary.completedInterviews },
  ] : [];

  const funnelChartData = funnel
    ? Object.entries(funnel.funnel).map(([key, value]) => ({
        label: key.charAt(0) + key.slice(1).toLowerCase().replace('_', ' '),
        value,
      }))
    : [];

  const trendChartData = trend
    ? trend.data.map((d) => ({
        label: d.date,
        value: d.count,
      }))
    : [];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Recruiter Analytics</h1>
          <p className="text-gray-500">Track your recruitment performance</p>
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
                <CardTitle>Application Funnel</CardTitle>
              </CardHeader>
              <CardContent>
                {funnelChartData.length > 0 ? (
                  <BarChart data={funnelChartData} height={250} />
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
                  <BarChart data={trendChartData} height={250} />
                ) : (
                  <p className="text-gray-500 text-center py-8">No data available</p>
                )}
              </CardContent>
            </Card>
          </div>

          {hiringMetrics && (
            <Card>
              <CardHeader>
                <CardTitle>Hiring Metrics</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <div>
                    <p className="text-sm text-gray-500">Shortlist Rate</p>
                    <p className="text-xl font-bold">{hiringMetrics.shortlistRate}%</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Hire Rate</p>
                    <p className="text-xl font-bold">{hiringMetrics.hireRate}%</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Total Hired</p>
                    <p className="text-xl font-bold">{hiringMetrics.totalHired}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Completed Interviews</p>
                    <p className="text-xl font-bold">{hiringMetrics.completedInterviews}</p>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}
        </>
      )}
    </div>
  );
}
