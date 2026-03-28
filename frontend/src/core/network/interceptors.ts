import axios, {
  type AxiosError,
  type AxiosInstance,
  type InternalAxiosRequestConfig,
} from 'axios';
import {
  ForbiddenError,
  NetworkError,
  UnauthorizedError,
  ValidationError,
} from '@core/error/AppError';
import { useAuthStore } from '@core/store/authStore';

interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}

interface ApiErrorResponse {
  message?: string;
  fields?: Record<string, string>;
}

interface RetryableConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

let refreshPromise: Promise<string> | null = null;

function toErrorMessage(
  error: AxiosError<ApiErrorResponse>,
  fallbackMessage: string,
): string {
  return error.response?.data?.message ?? fallbackMessage;
}

async function refreshAccessToken(baseUrl: string): Promise<string> {
  const { refreshToken, setAccessToken, clearAuth } = useAuthStore.getState();
  const refreshClient = axios.create({
    baseURL: baseUrl,
    timeout: 10_000,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (refreshToken === null) {
    clearAuth();
    throw new UnauthorizedError();
  }

  try {
    const response = await refreshClient.post<ApiResponse<{ accessToken: string }>>(
      '/auth/refresh',
      undefined,
      {
        headers: {
          Authorization: `Bearer ${refreshToken}`,
        },
      },
    );

    const accessToken = response.data.data.accessToken;
    setAccessToken(accessToken);
    return accessToken;
  } catch {
    clearAuth();
    throw new UnauthorizedError('세션이 만료되었습니다. 다시 로그인해주세요.');
  }
}

export function setupInterceptors(client: AxiosInstance): void {
  client.interceptors.request.use((config) => {
    const { accessToken } = useAuthStore.getState();

    config.headers = {
      'Content-Type': 'application/json',
      ...(config.headers ?? {}),
      ...(accessToken === null ? {} : { Authorization: `Bearer ${accessToken}` }),
    } as InternalAxiosRequestConfig['headers'];

    return config;
  });

  client.interceptors.response.use(
    (response) => response,
    async (error: AxiosError<ApiErrorResponse>) => {
      const status = error.response?.status;
      const originalConfig = error.config as RetryableConfig | undefined;
      const { clearAuth, refreshToken } = useAuthStore.getState();

      if (
        status === 401 &&
        refreshToken !== null &&
        originalConfig !== undefined &&
        originalConfig._retry !== true
      ) {
        originalConfig._retry = true;

        try {
          if (refreshPromise === null) {
            refreshPromise = refreshAccessToken(client.defaults.baseURL ?? '').finally(() => {
              refreshPromise = null;
            });
          }

          const nextAccessToken = await refreshPromise;
          originalConfig.headers = {
            ...(originalConfig.headers ?? {}),
            Authorization: `Bearer ${nextAccessToken}`,
          } as InternalAxiosRequestConfig['headers'];

          return client.request(originalConfig);
        } catch (refreshError) {
          clearAuth();
          return Promise.reject(refreshError);
        }
      }

      if (status === 401) {
        clearAuth();
        return Promise.reject(
          new UnauthorizedError(toErrorMessage(error, '인증이 필요합니다.')),
        );
      }

      if (status === 403) {
        return Promise.reject(
          new ForbiddenError(toErrorMessage(error, '접근 권한이 없습니다.')),
        );
      }

      if (status === 400) {
        return Promise.reject(
          new ValidationError(
            toErrorMessage(error, '입력값을 확인해주세요.'),
            error.response?.data?.fields,
          ),
        );
      }

      if (status !== undefined) {
        return Promise.reject(
          new NetworkError(toErrorMessage(error, `HTTP ${status}`), status),
        );
      }

      return Promise.reject(
        new NetworkError('인터넷 연결을 확인해주세요.', 0),
      );
    },
  );
}
