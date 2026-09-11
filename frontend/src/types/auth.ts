export type AuthRole = 'CANDIDATE' | 'RECRUITER' | 'ADMIN';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponseData {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  id: number;
  fullName: string;
  email: string;
  role: AuthRole;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  confirmPassword: string;
  role: 'CANDIDATE' | 'RECRUITER';
}

export interface RegisterResponseData {
  id: number;
  fullName: string;
  email: string;
  role: AuthRole;
  enabled: boolean;
  createdAt: string;
}

export interface AuthUser {
  id: number;
  email: string;
  fullName: string;
  role: AuthRole;
}

export interface AuthState {
  user: AuthUser | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}
