import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ForbiddenError } from '@core/error/AppError';
import { setupInterceptors } from '@core/network/interceptors';

const {
  axiosCreate,
  axiosPost,
  client,
  requestUse,
  responseUse,
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
  axiosPost: vi.fn(),
  requestUse: vi.fn(),
  responseUse: vi.fn(),
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
    post: axiosPost,
  },
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
    axiosPost.mockReset();
    getState.mockReset();
    getState.mockReturnValue({
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
});
