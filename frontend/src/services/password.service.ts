import api from './api';

interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  timestamp?: string;
}

const passwordService = {
  async changePassword(data: { currentPassword: string; newPassword: string; confirmPassword: string }): Promise<ApiResponse<null>> {
    const response = await api.post<ApiResponse<null>>('/auth/change-password', data);
    return response.data;
  },

  async forgotPassword(email: string): Promise<ApiResponse<null>> {
    const response = await api.post<ApiResponse<null>>('/auth/forgot-password', { email });
    return response.data;
  },

  async resetPassword(data: { token: string; newPassword: string; confirmPassword: string }): Promise<ApiResponse<null>> {
    const response = await api.post<ApiResponse<null>>('/auth/reset-password', data);
    return response.data;
  },
};

export default passwordService;
