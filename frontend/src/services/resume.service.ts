import { api, default as apiClient } from './api';

export interface Resume {
  id: number;
  originalFileName: string;
  contentType: string;
  fileSize: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export const resumeService = {
  async getResumes(): Promise<Resume[]> {
    const response = await api.get<Resume[]>('/candidates/me/resumes');
    return response.data!;
  },

  async getResumeById(resumeId: number): Promise<Resume> {
    const response = await api.get<Resume>(`/candidates/me/resumes/${resumeId}`);
    return response.data!;
  },

  async uploadResume(file: File): Promise<Resume> {
    const formData = new FormData();
    formData.append('file', file);
    const response = await apiClient.post('/candidates/me/resumes', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data.data;
  },

  async activateResume(resumeId: number): Promise<Resume> {
    const response = await api.post<Resume>(`/candidates/me/resumes/${resumeId}/activate`);
    return response.data!;
  },

  async deleteResume(resumeId: number): Promise<void> {
    await api.delete(`/candidates/me/resumes/${resumeId}`);
  },

  async downloadResume(resumeId: number, filename: string): Promise<void> {
    await api.downloadBlob(`/candidates/me/resumes/${resumeId}/download`, filename);
  },
};
