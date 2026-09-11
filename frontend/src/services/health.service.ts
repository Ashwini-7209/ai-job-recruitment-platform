import type { ApiResponse, HealthStatus } from '@/types/api';
import { api } from './api';

export const healthService = {
  check: async (): Promise<ApiResponse<HealthStatus>> => {
    return api.get<HealthStatus>('/health');
  },
};
