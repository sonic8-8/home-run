import { act, render } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AUTH_SESSION_EXPIRED_KEY } from '@core/network/sessionExpiry';
import { useAuthStore } from '@core/store/authStore';
import { refreshSession } from '@core/network/authSession';
import { useAuthSessionWatcher } from '@features/auth/presentation/hooks/useAuthSessionWatcher';

vi.mock('@core/network/authSession', () => ({
  refreshSession: vi.fn(),
}));

const initialState = useAuthStore.getState();

function resetAuthStore() {
  useAuthStore.setState(initialState);
  localStorage.clear();
  sessionStorage.clear();
}

function HookHarness() {
  useAuthSessionWatcher();
  return null;
}

describe('useAuthSessionWatcher', () => {
  beforeEach(() => {
    resetAuthStore();
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-03-28T00:00:00Z'));
    vi.mocked(refreshSession).mockReset();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('refreshes the session shortly before the access token expires', async () => {
    vi.mocked(refreshSession).mockResolvedValue({
      accessToken: 'refreshed-token',
      accessTokenExpiresIn: 1800,
    });

    act(() => {
      useAuthStore.getState().setAuth('old-token', 'refresh-token', 61, '테스터');
    });

    render(<HookHarness />);

    await act(async () => {
      await vi.advanceTimersByTimeAsync(1000);
      await Promise.resolve();
    });

    expect(refreshSession).toHaveBeenCalledTimes(1);
    expect(useAuthStore.getState().accessToken).toBe('refreshed-token');
    expect(useAuthStore.getState().accessTokenExpiresAt).toBeGreaterThan(Date.now());
  });

  it('refreshes legacy persisted sessions immediately when expiry is missing', async () => {
    vi.mocked(refreshSession).mockResolvedValue({
      accessToken: 'refreshed-token',
      accessTokenExpiresIn: 1800,
    });

    act(() => {
      useAuthStore.setState({
        ...useAuthStore.getState(),
        isAuthenticated: true,
        accessToken: 'legacy-access-token',
        refreshToken: 'refresh-token',
        nickname: '테스터',
        accessTokenExpiresAt: null,
      });
    });

    render(<HookHarness />);

    await act(async () => {
      await vi.advanceTimersByTimeAsync(0);
      await Promise.resolve();
    });

    expect(refreshSession).toHaveBeenCalledTimes(1);
    expect(useAuthStore.getState().accessToken).toBe('refreshed-token');
  });

  it('clears auth and stores an expiry notice when refresh fails', async () => {
    vi.mocked(refreshSession).mockRejectedValue(new Error('refresh failed'));

    act(() => {
      useAuthStore.getState().setAuth('old-token', 'refresh-token', 60, '테스터');
    });

    render(<HookHarness />);

    await act(async () => {
      await vi.advanceTimersByTimeAsync(0);
      await Promise.resolve();
    });

    expect(useAuthStore.getState().isAuthenticated).toBe(false);
    expect(sessionStorage.getItem(AUTH_SESSION_EXPIRED_KEY)).toBe('1');
  });
});
