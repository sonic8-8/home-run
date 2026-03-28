import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ForbiddenError } from '@core/error/AppError';
import { setupInterceptors } from '@core/network/interceptors';

const {
  axiosCreate,
  client,
  requestUse,
  responseUse,
  refreshSession,
  expireSession,
  getState,
} = vi.hoisted(() => ({
  client: {
    defaults: { baseURL: '' },
    interceptors: {
      request: { use: vi.fn() },
      response: { use: vi.fn() },
    },
  },
  axiosCreate: vi.fn(),
  requestUse: vi.fn(),
  responseUse: vi.fn(),
  refreshSession: vi.fn(),
  expireSession: vi.fn(),
  getState: vi.fn(),
}));

client.interceptors.request.use = requestUse;
client.interceptors.response.use = responseUse;

vi.mock('axios', () => ({
  __esModule: true,
  default: {
    create: (...args: unknown[]) => {
      axiosCreate(...args);
      if (typeof args[0] === 'object' && args[0] !== null) {
        client.defaults = {
          ...client.defaults,
          ...(args[0] as Record<string, unknown>),
        };
      }
      return client;
    },
  },
}));

vi.mock('@core/network/authSession', () => ({
  refreshSession,
}));

vi.mock('@core/network/sessionExpiry', () => ({
  expireSession,
  AUTH_SESSION_EXPIRED_KEY: 'auth/session-expired',
}));

vi.mock('@core/store/authStore', () => ({
  useAuthStore: {
    getState,
  },
}));

import { apiClient } from '@core/network/apiClient';

describe('apiClient', () => {
  beforeEach(() => {
    requestUse.mockReset();
    responseUse.mockReset();
    refreshSession.mockReset();
    expireSession.mockReset();
    getState.mockReset();
    getState.mockReturnValue({
      isAuthenticated: false,
      accessToken: null,
      refreshToken: null,
      setAccessToken: vi.fn(),
      clearAuth: vi.fn(),
    });
    setupInterceptors(apiClient);
  });

  it('creates the axios client with the shared timeout budget', () => {
    expect(apiClient.defaults.timeout).toBe(10_000);
  });

  it('exports the raw axios instance without data unwrapping helpers', () => {
    expect(apiClient).toBe(client);
  });

  it('registers a response interceptor that maps 403 errors to ForbiddenError', async () => {
    const onRejected = responseUse.mock.calls[0]?.[1];

    await expect(
      onRejected({
        response: {
          status: 403,
          data: {
            message: '권한이 없습니다.',
          },
        },
        config: {},
      }),
    ).rejects.toEqual(new ForbiddenError('권한이 없습니다.'));
  });

  it('expires the session when token refresh fails on a 401 response', async () => {
    const onRejected = responseUse.mock.calls[0]?.[1];

    getState.mockReturnValue({
      isAuthenticated: true,
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      setAccessToken: vi.fn(),
      clearAuth: vi.fn(),
    });
    refreshSession.mockRejectedValue(new Error('refresh failed'));

    await expect(
      onRejected({
        response: {
          status: 401,
          data: {
            message: '인증이 필요합니다.',
          },
        },
        config: {
          headers: {},
        },
      }),
    ).rejects.toThrow('refresh failed');

    expect(expireSession).toHaveBeenCalledTimes(1);
  });
});
