import { useAuthStore } from '@core/store/authStore';

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}

// 토큰 갱신 중 중복 요청 방지
let refreshPromise: Promise<string> | null = null;

async function refreshAccessToken(): Promise<string> {
  const { refreshToken, setAccessToken, clearAuth } = useAuthStore.getState();

  if (!refreshToken) {
    clearAuth();
    throw new Error('No refresh token');
  }

  const res = await fetch(`${BASE_URL}/api/auth/refresh`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${refreshToken}` },
  });

  if (!res.ok) {
    clearAuth();
    throw new Error('Token refresh failed');
  }

  const body: ApiResponse<{ accessToken: string }> = await res.json();
  setAccessToken(body.data.accessToken);
  return body.data.accessToken;
}

async function request<T>(
  method: string,
  path: string,
  body?: unknown,
): Promise<T> {
  const { accessToken } = useAuthStore.getState();

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  };
  if (accessToken) {
    headers['Authorization'] = `Bearer ${accessToken}`;
  }

  const init: RequestInit = {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  };

  let res = await fetch(`${BASE_URL}${path}`, init);

  // 401 → 리프레시 토큰이 있을 때만 갱신 후 1회 재시도
  if (res.status === 401 && useAuthStore.getState().refreshToken) {
    if (!refreshPromise) {
      refreshPromise = refreshAccessToken().finally(() => {
        refreshPromise = null;
      });
    }

    const newToken = await refreshPromise;
    headers['Authorization'] = `Bearer ${newToken}`;
    res = await fetch(`${BASE_URL}${path}`, { ...init, headers });
  }

  if (!res.ok) {
    const errorBody = await res.json().catch(() => ({}));
    throw new Error(
      (errorBody as { message?: string }).message ?? `HTTP ${res.status}`,
    );
  }

  if (res.status === 204 || res.headers.get('content-length') === '0') {
    return undefined as T;
  }

  const json: ApiResponse<T> = await res.json();
  return json.data;
}

export const apiClient = {
  get: <T>(path: string) => request<T>('GET', path),
  post: <T>(path: string, body?: unknown) => request<T>('POST', path, body),
  put: <T>(path: string, body?: unknown) => request<T>('PUT', path, body),
  delete: <T>(path: string) => request<T>('DELETE', path),
};
