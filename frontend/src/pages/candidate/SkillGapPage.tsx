import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Skeleton } from '@/components/ui/Skeleton';
import { aiService } from '@/services/ai.service';
import type { SkillGapResponse } from '@/services/ai.service';

export default function SkillGapPage() {
  const { jobId } = useParams<{ jobId: string }>();
  const [gap, setGap] = useState<SkillGapResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (jobId) {
      loadGap(parseInt(jobId));
    }
  }, [jobId]);

  const loadGap = async (id: number) => {
    setLoading(true);
    setError(false);
    try {
      const res = await aiService.getSkillGap(id);
      setGap(res.data ?? null);
    } catch {
      setError(true);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Skill Gap Analysis</h1>
        <p className="text-gray-500">See how your skills match the job requirements</p>
      </div>

      {loading ? (
        <div className="space-y-4">
          <Skeleton className="h-48" />
          <Skeleton className="h-48" />
        </div>
      ) : error ? (
        <Card>
          <CardContent className="py-12 text-center">
            <p className="text-gray-500">Unable to load skill gap analysis</p>
          </CardContent>
        </Card>
      ) : gap ? (
        <>
          <div className="flex items-center gap-2">
            <h2 className="text-lg font-semibold">{gap.jobTitle}</h2>
            {gap.aiEnhanced && <Badge variant="info">AI-enhanced</Badge>}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <Card>
              <CardHeader>
                <CardTitle className="text-green-600">Matched Skills</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex flex-wrap gap-2">
                  {(gap.matchedSkills || []).map((s, i) => (
                    <Badge key={i} variant="success">{s}</Badge>
                  ))}
                  {(gap.matchedSkills || []).length === 0 && (
                    <p className="text-sm text-gray-500">No matched skills yet</p>
                  )}
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-red-600">Missing Skills</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex flex-wrap gap-2">
                  {(gap.missingSkills || []).map((s, i) => (
                    <Badge key={i} variant="error">{s}</Badge>
                  ))}
                  {(gap.missingSkills || []).length === 0 && (
                    <p className="text-sm text-gray-500">No missing skills</p>
                  )}
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-yellow-600">Partially Matched</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex flex-wrap gap-2">
                  {(gap.partiallyMatchedSkills || []).map((s, i) => (
                    <Badge key={i} variant="warning">{s}</Badge>
                  ))}
                  {(gap.partiallyMatchedSkills || []).length === 0 && (
                    <p className="text-sm text-gray-500">None</p>
                  )}
                </div>
              </CardContent>
            </Card>
          </div>

          {gap.explanation && (
            <Card>
              <CardContent className="py-4">
                <p className="text-sm text-gray-600">{gap.explanation}</p>
              </CardContent>
            </Card>
          )}

          {(gap.prioritySuggestions || []).length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle>Priority Suggestions</CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2">
                  {(gap.prioritySuggestions || []).map((s, i) => (
                    <li key={i} className="flex items-start gap-2 text-sm">
                      <span className="text-blue-500 mt-1">•</span>
                      {s}
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          )}

          <Card>
            <CardHeader>
              <CardTitle>All Required Skills</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex flex-wrap gap-2">
                {(gap.jobRequiredSkills || []).map((s, i) => (
                  <Badge key={i} variant={(gap.matchedSkills || []).includes(s) ? 'success' : 'default'}>
                    {s}
                  </Badge>
                ))}
              </div>
            </CardContent>
          </Card>
        </>
      ) : null}
    </div>
  );
}
