import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { AUTH_SESSION_EXPIRED_KEY } from '@core/network/sessionExpiry';
import { useAuthStore } from '@core/store/authStore';
import {
  AUTH_SESSION_EXPIRED_MESSAGE,
} from '@features/auth/presentation/constants/session';
import { AuthPage } from '@features/auth/presentation/pages/AuthPage';

const { resolveMock } = vi.hoisted(() => ({
  resolveMock: vi.fn(),
}));

vi.mock('@core/di/container', () => ({
  container: {
    resolve: resolveMock,
  },
}));

const initialState = useAuthStore.getState();

function resetAuthStore() {
  useAuthStore.setState(initialState);
  localStorage.clear();
  sessionStorage.clear();
}

function renderAuthPage(
  initialEntry:
    | string
    | {
        pathname: string;
        state?: unknown;
      },
) {
  return render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <Routes>
        <Route path="/login" element={<AuthPage />} />
        <Route path="/" element={<div>Home Page</div>} />
        <Route path="/loan" element={<div>Loan Page</div>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('useAuth', () => {
  beforeEach(() => {
    resetAuthStore();
    resolveMock.mockReset();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('shows the session expired notice once on the login page', async () => {
    sessionStorage.setItem(AUTH_SESSION_EXPIRED_KEY, '1');

    renderAuthPage('/login');

    expect(await screen.findByText(AUTH_SESSION_EXPIRED_MESSAGE)).toBeInTheDocument();
    expect(sessionStorage.getItem(AUTH_SESSION_EXPIRED_KEY)).toBeNull();
  });

  it('returns the user to the saved private route after login', async () => {
    const execute = vi.fn().mockResolvedValue({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      accessTokenExpiresIn: 1800,
      name: '테스터',
    });
    const user = userEvent.setup();

    resolveMock.mockReturnValue({ execute });
    renderAuthPage({
      pathname: '/login',
      state: {
        from: {
          pathname: '/loan',
        },
      },
    });

    await user.type(screen.getByPlaceholderText('example@email.com'), 'tester@example.com');
    await user.type(screen.getByPlaceholderText('Password'), 'Password123!');
    await user.click(screen.getByRole('button', { name: '로그인' }));

    expect(await screen.findByText('Loan Page')).toBeInTheDocument();
    await waitFor(() => {
      expect(useAuthStore.getState().accessToken).toBe('access-token');
    });
    expect(execute).toHaveBeenCalledWith({
      email: 'tester@example.com',
      password: 'Password123!',
    });
  });
});
