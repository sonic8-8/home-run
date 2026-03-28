import { act } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { useAuthStore } from '@core/store/authStore';

const initialState = useAuthStore.getState();

function resetAuthStore() {
  useAuthStore.setState(initialState);
  localStorage.clear();
}

describe('useAuthStore', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-03-28T00:00:00Z'));
    resetAuthStore();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('stores auth tokens and nickname when setAuth is called', () => {
    act(() => {
      useAuthStore.getState().setAuth('access-token', 'refresh-token', 1800, '테스터');
    });

    expect(useAuthStore.getState()).toMatchObject({
      isAuthenticated: true,
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      nickname: '테스터',
      accessTokenExpiresAt: Date.parse('2026-03-28T00:30:00Z'),
    });

    const persisted = JSON.parse(localStorage.getItem('auth') ?? '{}');

    expect(persisted.state).toMatchObject({
      isAuthenticated: true,
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      nickname: '테스터',
      accessTokenExpiresAt: Date.parse('2026-03-28T00:30:00Z'),
    });
  });

  it('updates only the access token when setAccessToken is called', () => {
    act(() => {
      useAuthStore.getState().setAuth('old-access', 'refresh-token', 60, '테스터');
      useAuthStore.getState().setAccessToken('new-access', 1800);
    });

    expect(useAuthStore.getState()).toMatchObject({
      isAuthenticated: true,
      accessToken: 'new-access',
      refreshToken: 'refresh-token',
      nickname: '테스터',
      accessTokenExpiresAt: Date.parse('2026-03-28T00:30:00Z'),
    });
  });

  it('keeps legacy persisted auth sessions refreshable until the watcher repairs expiry', () => {
    act(() => {
      useAuthStore.setState({
        ...useAuthStore.getState(),
        isAuthenticated: true,
        accessToken: 'legacy-access',
        refreshToken: 'legacy-refresh',
        nickname: '테스터',
        accessTokenExpiresAt: null,
      });
    });

    expect(useAuthStore.getState()).toMatchObject({
      isAuthenticated: true,
      accessToken: 'legacy-access',
      refreshToken: 'legacy-refresh',
      nickname: '테스터',
      accessTokenExpiresAt: null,
    });
  });

  it('clears the persisted auth state when clearAuth is called', () => {
    act(() => {
      useAuthStore.getState().setAuth('access-token', 'refresh-token', 1800, '테스터');
      useAuthStore.getState().clearAuth();
    });

    expect(useAuthStore.getState()).toMatchObject({
      isAuthenticated: false,
      accessToken: null,
      refreshToken: null,
      nickname: null,
      accessTokenExpiresAt: null,
    });

    const persisted = JSON.parse(localStorage.getItem('auth') ?? '{}');

    expect(persisted.state).toMatchObject({
      isAuthenticated: false,
      accessToken: null,
      refreshToken: null,
      nickname: null,
      accessTokenExpiresAt: null,
    });
  });
});
