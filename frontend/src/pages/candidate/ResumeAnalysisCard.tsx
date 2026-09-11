import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Skeleton } from '@/components/ui/Skeleton';
import { aiService } from '@/services/ai.service';
import type { ResumeImprovementResponse } from '@/services/ai.service';

interface ResumeAnalysisProps {
  resumeId: number;
}

export default function ResumeAnalysisCard({ resumeId }: ResumeAnalysisProps) {
  const [analysis, setAnalysis] = useState<ResumeImprovementResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);

  const analyze = async () => {
    setLoading(true);
    setError(false);
    try {
      const res = await aiService.analyzeResumeImprovement(resumeId);
      setAnalysis(res.data ?? null);
    } catch {
      setError(true);
    } finally {
      setLoading(false);
    }
  };

  if (!analysis && !loading) {
    return (
      <Card>
        <CardContent className="py-6 text-center">
          <Button onClick={analyze}>Analyze Resume</Button>
          <p className="text-xs text-gray-500 mt-2">AI will analyze your resume for improvements</p>
        </CardContent>
      </Card>
    );
  }

  if (loading) {
    return <Skeleton className="h-48" />;
  }

  if (error) {
    return (
      <Card>
        <CardContent className="py-6 text-center">
          <p className="text-sm text-red-500 mb-2">Analysis failed</p>
          <Button onClick={analyze} variant="outline" size="sm">Retry</Button>
        </CardContent>
      </Card>
    );
  }

  if (!analysis) return null;

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Resume Analysis</CardTitle>
          <div className="flex items-center gap-2">
            {analysis.aiEnhanced && <Badge variant="info">AI-assisted</Badge>}
            <span className="text-sm font-medium">{analysis.completeness}% complete</span>
          </div>
        </div>
        <div className="w-full h-2 bg-gray-100 rounded-full mt-2">
          <div
            className="h-full bg-blue-500 rounded-full"
            style={{ width: `${analysis.completeness}%` }}
          />
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        {analysis.strengths.length > 0 && (
          <div>
            <h4 className="text-sm font-medium text-green-600 mb-2">Strengths</h4>
            <ul className="space-y-1">
              {analysis.strengths.map((s, i) => (
                <li key={i} className="text-sm flex items-start gap-2">
                  <span className="text-green-500">+</span>{s}
                </li>
              ))}
            </ul>
          </div>
        )}

        {analysis.improvements.length > 0 && (
          <div>
            <h4 className="text-sm font-medium text-yellow-600 mb-2">Improvements</h4>
            <ul className="space-y-1">
              {analysis.improvements.map((s, i) => (
                <li key={i} className="text-sm flex items-start gap-2">
                  <span className="text-yellow-500">!</span>{s}
                </li>
              ))}
            </ul>
          </div>
        )}

        {analysis.missingSections.length > 0 && (
          <div>
            <h4 className="text-sm font-medium text-orange-600 mb-2">Missing Sections</h4>
            <ul className="space-y-1">
              {analysis.missingSections.map((s, i) => (
                <li key={i} className="text-sm flex items-start gap-2">
                  <span className="text-orange-500">?</span>{s}
                </li>
              ))}
            </ul>
          </div>
        )}

        {analysis.keywordSuggestions.length > 0 && (
          <div>
            <h4 className="text-sm font-medium text-blue-600 mb-2">Keyword Suggestions</h4>
            <ul className="space-y-1">
              {analysis.keywordSuggestions.map((s, i) => (
                <li key={i} className="text-sm flex items-start gap-2">
                  <span className="text-blue-500">→</span>{s}
                </li>
              ))}
            </ul>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
