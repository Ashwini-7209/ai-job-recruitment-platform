import { useState, useCallback } from 'react';
import { savedJobService } from '@/services/savedJob.service';

export function useSaveJob(initialSaved: boolean = false) {
  const [isSaved, setIsSaved] = useState(initialSaved);
  const [isLoading, setIsLoading] = useState(false);

  const save = useCallback(async (jobId: number) => {
    if (isLoading) return;
    setIsLoading(true);
    try {
      await savedJobService.saveJob(jobId);
      setIsSaved(true);
    } catch {
      // Error handled silently
    } finally {
      setIsLoading(false);
    }
  }, [isLoading]);

  const unsave = useCallback(async (jobId: number) => {
    if (isLoading) return;
    setIsLoading(true);
    try {
      await savedJobService.unsaveJob(jobId);
      setIsSaved(false);
    } catch {
      // Error handled silently
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

  return { isSaved, isLoading, save, unsave, toggle, setIsSaved };
}
