import { render, renderHook, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { ROUTES } from '@app/routes';
import { useGameMain } from './useGameMain';

const mockNavigate = vi.fn();
const mockResolve = vi.fn();

interface MockLocationState {
  sessionId?: number;
  characterType?: string;
  openLoan?: boolean;
}

const mockLocation: { key: string; state: MockLocationState } = {
  key: 'game-main',
  state: {
    sessionId: 7,
    characterType: 'MALE',
  },
};

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useLocation: () => mockLocation,
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
    turn: {
      turnNumber: 7,
      month: 3,
      currentDate: new Date('2026-03-01T00:00:00'),
      economicCycle: {
        phase: 'BOOM',
        description: '경기 호황기',
      },
      runtimeSnapshot: {
        assets: {
          cashBalance: 2_300_000,
          netWorth: 18_700_000,
        },
        stats: {
          health: 72,
          fatigue: 28,
          stress: 34,
          happiness: 61,
          knowledge: 55,
        },
      },
    },
    news: null,
    newsHistory: [],
    pendingEvents: [],
    resolvedEvent: null,
    turnActions: null,
    turnPreview: null,
    turnCommitResult: null,
    isTurnLoading: false,
    turnError: null,
    isNewsLoading: false,
    newsError: null,
    isNewsHistoryLoading: false,
    newsHistoryError: null,
    isActionsLoading: false,
    isSlotSubmitting: false,
    isTurnCommitting: false,
    isPendingEventsLoading: false,
    isEventResolving: false,
    eventError: null,
    scheduleError: null,
    fetchTurn: vi.fn().mockResolvedValue({
      turnNumber: 7,
      month: 3,
      currentDate: new Date('2026-03-01T00:00:00'),
      economicCycle: {
        phase: 'BOOM',
        description: '경기 호황기',
      },
      runtimeSnapshot: {
        assets: {
          cashBalance: 2_300_000,
          netWorth: 18_700_000,
        },
        stats: {
          health: 72,
          fatigue: 28,
          stress: 34,
          happiness: 61,
          knowledge: 55,
        },
      },
    }),
    fetchLatestNews: vi.fn(),
    fetchNewsHistory: vi.fn(async () => []),
    fetchPendingEvents: vi.fn(async () => []),
    fetchTurnActions: vi.fn(),
    resolvePendingEvent: vi.fn(async () => null),
    submitTurnSlots: vi.fn(),
    commitTurn: vi.fn(),
    dismissResolvedEvent: vi.fn(),
    resetPendingEventFlow: vi.fn(),
    resetScheduleFlow: vi.fn(),
  }),
}));

function HookHarness() {
  useGameMain();
  return null;
}

describe('useGameMain', () => {
  it('opens the loan panel immediately when the route requests openLoan', async () => {
    mockLocation.key = 'game-main-loan';
    mockLocation.state = {
      sessionId: 7,
      characterType: 'MALE',
      openLoan: true,
    };

    const { result } = renderHook(() => useGameMain());

    await waitFor(() => {
      expect(result.current.leftView).toBe('loan');
    });
  });

  it('redirects terminal sessions to the ending page', async () => {
    mockLocation.key = 'game-main';
    mockLocation.state = {
      sessionId: 7,
      characterType: 'MALE',
    };
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
    mockLocation.key = 'game-main';
    mockLocation.state = {
      sessionId: 7,
      characterType: 'MALE',
    };
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
