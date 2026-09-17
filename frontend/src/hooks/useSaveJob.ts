import { useState, useCallback } from 'react';
import { savedJobService } from '@/services/savedJob.service';

export function useSaveJob(initialSaved: boolean = false) {
  const [isSaved, setIsSaved] = useState(initialSaved);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const save = useCallback(async (jobId: number) => {
    if (isLoading) return;
    setIsLoading(true);
    setError(null);
    try {
      await savedJobService.saveJob(jobId);
      setIsSaved(true);
    } catch {
      setError('Failed to save job. Please try again.');
    } finally {
      setIsLoading(false);
    }
  }, [isLoading]);

  const unsave = useCallback(async (jobId: number) => {
    if (isLoading) return;
    setIsLoading(true);
    setError(null);
    try {
      await savedJobService.unsaveJob(jobId);
      setIsSaved(false);
    } catch {
      setError('Failed to remove saved job. Please try again.');
    } finally {
      setIsLoading(false);
    }
  }, [isLoading]);

  const toggle = useCallback(async (jobId: number) => {
    if (isSaved) {
      await unsave(jobId);
    } else {
      await save(jobId);
    }
  }, [isSaved, save, unsave]);

  return { isSaved, isLoading, error, save, unsave, toggle, setIsSaved };
}
