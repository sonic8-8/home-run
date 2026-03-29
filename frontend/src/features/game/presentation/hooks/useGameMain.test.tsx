import { render, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { ROUTES } from '@app/routes';
import { useGameMain } from './useGameMain';

const mockNavigate = vi.fn();
const mockResolve = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useLocation: () => ({
      state: {
        sessionId: 7,
        characterType: 'MALE',
      },
    }),
  };
});

vi.mock('@core/di/container', () => ({
  container: {
    resolve: (...args: unknown[]) => mockResolve(...args),
  },
}));

vi.mock('./useCreateGameSession', () => ({
  useCreateGameSession: () => ({
    createSession: vi.fn(),
    isSubmitting: false,
    error: null,
  }),
}));

vi.mock('@features/game/presentation/hooks/useGameTurn', () => ({
  useGameTurn: () => ({
    turn: { currentDate: new Date('2026-03-01T00:00:00') },
    news: null,
    isTurnLoading: false,
    turnError: null,
    isNewsLoading: false,
    newsError: null,
    fetchTurn: vi.fn().mockResolvedValue({ currentDate: new Date('2026-03-01T00:00:00') }),
    fetchLatestNews: vi.fn(),
  }),
}));

function HookHarness() {
  useGameMain();
  return null;
}

describe('useGameMain', () => {
  it('redirects terminal sessions to the ending page', async () => {
    mockNavigate.mockReset();
    mockResolve.mockReset();
    mockResolve.mockReturnValue({
      execute: vi.fn().mockResolvedValue({
        sessionId: 7,
        status: 'CLEAR',
      }),
    });

    render(<HookHarness />);

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith(ROUTES.GAME_ENDING(7), { replace: true });
    });
  });

  it('does not redirect when the session is still in progress', async () => {
    mockNavigate.mockReset();
    mockResolve.mockReset();
    mockResolve.mockReturnValue({
      execute: vi.fn().mockResolvedValue({
        sessionId: 7,
        status: 'IN_PROGRESS',
      }),
    });

    render(<HookHarness />);

    await waitFor(() => {
      expect(mockResolve).toHaveBeenCalled();
    });

    expect(mockNavigate).not.toHaveBeenCalledWith(ROUTES.GAME_ENDING(7), { replace: true });
  });
});
