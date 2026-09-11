import { api } from '@/services/api';
import type {
  LoginRequest,
  LoginResponseData,
  RegisterRequest,
  RegisterResponseData,
  AuthUser,
} from '@/types/auth';
import type { ApiResponse } from '@/types/api';

const authService = {
  async login(data: LoginRequest): Promise<ApiResponse<LoginResponseData>> {
    return api.post<LoginResponseData>('/auth/login', data);
  },

  async register(
    data: RegisterRequest,
  ): Promise<ApiResponse<RegisterResponseData>> {
    return api.post<RegisterResponseData>('/auth/register', data);
  },

  async registerCandidate(
    data: Omit<RegisterRequest, 'role'>,
  ): Promise<ApiResponse<RegisterResponseData>> {
    return api.post<RegisterResponseData>('/auth/register', {
      ...data,
      role: 'CANDIDATE',
    });
  },

  async registerRecruiter(
    data: Omit<RegisterRequest, 'role'>,
  ): Promise<ApiResponse<RegisterResponseData>> {
    return api.post<RegisterResponseData>('/auth/register', {
      ...data,
      role: 'RECRUITER',
    });
  },

  logout(): void {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_user');
  },

  getStoredUser(): AuthUser | null {
    try {
      const raw = localStorage.getItem('auth_user');
      if (!raw) return null;
      const parsed = JSON.parse(raw) as AuthUser;
      if (parsed && parsed.id && parsed.email && parsed.role) return parsed;
      return null;
    } catch {
      return null;
    }
  },

  getStoredToken(): string | null {
    return localStorage.getItem('auth_token');
  },
};

export default authService;
