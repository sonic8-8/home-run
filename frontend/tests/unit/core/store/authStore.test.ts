import { act } from '@testing-library/react';
import { beforeEach, describe, expect, it } from 'vitest';
import { useAuthStore } from '@core/store/authStore';

const initialState = useAuthStore.getState();

function resetAuthStore() {
  useAuthStore.setState(initialState);
  localStorage.clear();
}

describe('useAuthStore', () => {
  beforeEach(() => {
    resetAuthStore();
  });

  it('stores auth tokens and nickname when setAuth is called', () => {
    act(() => {
      useAuthStore.getState().setAuth('access-token', 'refresh-token', '테스터');
    });

    expect(useAuthStore.getState()).toMatchObject({
      isAuthenticated: true,
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      nickname: '테스터',
    });

    const persisted = JSON.parse(localStorage.getItem('auth') ?? '{}');

    expect(persisted.state).toMatchObject({
      isAuthenticated: true,
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      nickname: '테스터',
    });
  });

  it('updates only the access token when setAccessToken is called', () => {
    act(() => {
      useAuthStore.getState().setAuth('old-access', 'refresh-token', '테스터');
      useAuthStore.getState().setAccessToken('new-access');
    });

    expect(useAuthStore.getState()).toMatchObject({
      isAuthenticated: true,
      accessToken: 'new-access',
      refreshToken: 'refresh-token',
      nickname: '테스터',
    });
  });

  it('clears the persisted auth state when clearAuth is called', () => {
    act(() => {
      useAuthStore.getState().setAuth('access-token', 'refresh-token', '테스터');
      useAuthStore.getState().clearAuth();
    });

    expect(useAuthStore.getState()).toMatchObject({
      isAuthenticated: false,
      accessToken: null,
      refreshToken: null,
      nickname: null,
    });

    const persisted = JSON.parse(localStorage.getItem('auth') ?? '{}');

    expect(persisted.state).toMatchObject({
      isAuthenticated: false,
      accessToken: null,
      refreshToken: null,
      nickname: null,
    });
  });
});
