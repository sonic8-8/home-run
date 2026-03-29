import axios from 'axios';
import { UnauthorizedError } from '@core/error/AppError';
import { useAuthStore } from '@core/store/authStore';

interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}

export interface RefreshSessionResult {
  accessToken: string;
  accessTokenExpiresIn: number;
}

let refreshPromise: Promise<RefreshSessionResult> | null = null;

async function requestRefresh(
  baseUrl: string,
  refreshToken: string,
): Promise<RefreshSessionResult> {
  const refreshClient = axios.create({
    baseURL: baseUrl,
    timeout: 10_000,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  const response = await refreshClient.post<ApiResponse<RefreshSessionResult>>(
    '/auth/refresh',
    undefined,
    {
      headers: {
        Authorization: `Bearer ${refreshToken}`,
      },
    },
  );

  return response.data.data;
}

export function refreshSession(baseUrl: string): Promise<RefreshSessionResult> {
  const { refreshToken } = useAuthStore.getState();

  if (refreshToken === null) {
    return Promise.reject(new UnauthorizedError());
  }

  if (refreshPromise === null) {
    refreshPromise = requestRefresh(baseUrl, refreshToken).finally(() => {
      refreshPromise = null;
    });
  }

  return refreshPromise;
}
