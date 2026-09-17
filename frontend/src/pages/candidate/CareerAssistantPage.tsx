import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Skeleton } from '@/components/ui/Skeleton';
import { aiService } from '@/services/ai.service';
import type { CareerInsightsResponse } from '@/services/ai.service';

export default function CareerAssistantPage() {
  const [insights, setInsights] = useState<CareerInsightsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    loadInsights();
  }, []);

  const loadInsights = async () => {
    setLoading(true);
    setError(false);
    try {
      const res = await aiService.getCareerInsights();
      setInsights(res.data ?? null);
    } catch {
      setError(true);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">AI Career Assistant</h1>
          <p className="text-gray-500">Personalized insights to advance your career</p>
        </div>
        <Button onClick={loadInsights} variant="outline" disabled={loading}>
          Refresh
        </Button>
      </div>

      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {[...Array(6)].map((_, i) => <Skeleton key={i} className="h-48" />)}
        </div>
      ) : error ? (
        <Card>
          <CardContent className="py-12 text-center">
            <p className="text-gray-500 mb-4">Unable to load career insights</p>
            <Button onClick={loadInsights}>Try Again</Button>
          </CardContent>
        </Card>
      ) : insights ? (
        <>
          {insights.aiEnhanced && (
            <Badge variant="info" className="mb-2">AI-assisted suggestion</Badge>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <CardTitle>Your Strengths</CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2">
                  {(insights.strengths || []).map((s, i) => (
                    <li key={i} className="flex items-start gap-2 text-sm">
                      <span className="text-green-500 mt-1">+</span>
                      {s}
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Recommended Skills</CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2">
                  {(insights.recommendedSkills || []).map((s, i) => (
                    <li key={i} className="flex items-start gap-2 text-sm">
                      <span className="text-blue-500 mt-1">•</span>
                      {s}
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Suitable Job Categories</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex flex-wrap gap-2">
                  {(insights.suggestedJobCategories || []).map((c, i) => (
                    <Badge key={i} variant="default">{c}</Badge>
                  ))}
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Profile Improvements</CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2">
                  {(insights.profileImprovements || []).map((s, i) => (
                    <li key={i} className="flex items-start gap-2 text-sm">
                      <span className="text-yellow-500 mt-1">!</span>
                      {s}
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Resume Improvements</CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2">
                  {(insights.resumeImprovements || []).map((s, i) => (
                    <li key={i} className="flex items-start gap-2 text-sm">
                      <span className="text-orange-500 mt-1">?</span>
                      {s}
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Career Suggestions</CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2">
                  {(insights.generalCareerSuggestions || []).map((s, i) => (
                    <li key={i} className="flex items-start gap-2 text-sm">
                      <span className="text-purple-500 mt-1">→</span>
                      {s}
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          </div>
        </>
      ) : null}
    </div>
  );
}
