import {
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
import { refreshSession } from '@core/network/authSession';
import { expireSession } from '@core/network/sessionExpiry';
import { useAuthStore } from '@core/store/authStore';

interface ApiErrorResponse {
  message?: string;
  fields?: Record<string, string>;
}

interface RetryableConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

function toErrorMessage(
  error: AxiosError<ApiErrorResponse>,
  fallbackMessage: string,
): string {
  return error.response?.data?.message ?? fallbackMessage;
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
      const {
        clearAuth,
        isAuthenticated,
        refreshToken,
        setAccessToken,
      } = useAuthStore.getState();

      if (
        status === 401 &&
        refreshToken !== null &&
        originalConfig !== undefined &&
        originalConfig._retry !== true
      ) {
        originalConfig._retry = true;

        try {
          const nextSession = await refreshSession(client.defaults.baseURL ?? '');
          setAccessToken(nextSession.accessToken, nextSession.accessTokenExpiresIn);
          originalConfig.headers = {
            ...(originalConfig.headers ?? {}),
            Authorization: `Bearer ${nextSession.accessToken}`,
          } as InternalAxiosRequestConfig['headers'];

          return client.request(originalConfig);
        } catch (refreshError) {
          expireSession();
          return Promise.reject(refreshError);
        }
      }

      if (status === 401) {
        if (isAuthenticated) {
          expireSession();
        } else {
          clearAuth();
        }
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
