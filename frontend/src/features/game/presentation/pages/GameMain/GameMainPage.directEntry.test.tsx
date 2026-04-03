import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import { container } from '@core/di/container';
import { GetGameSessionDetailUseCase } from '@features/game/domain/usecases/GetGameSessionDetailUseCase';
import { GetGameSlotsUseCase } from '@features/game/domain/usecases/GetGameSlotsUseCase';
import { GameMainPage } from './GameMainPage';
import { useGameGuide } from '@features/game/presentation/hooks/useGameGuide';
import { useGameTurn } from '@features/game/presentation/hooks/useGameTurn';

vi.mock('@features/game/presentation/hooks/useGameTurn', () => ({
  useGameTurn: vi.fn(),
}));

vi.mock('@features/game/presentation/hooks/useGameGuide', () => ({
  useGameGuide: vi.fn(),
}));

vi.mock('@core/di/container', () => ({
  container: {
    resolve: vi.fn(),
  },
}));

vi.mock('@features/game/presentation/components/NewsEventModal/NewsEventModal', () => ({
  NewsEventModal: ({ isOpen }: { isOpen: boolean }) => (isOpen ? <div>뉴스 모달</div> : null),
}));

vi.mock('@features/game/presentation/components/MonthlyActivityModal/MonthlyActivityModal', () => ({
  MonthlyActivityModal: ({ isOpen }: { isOpen: boolean }) => (
    isOpen ? <div>월 활동 모달</div> : null
  ),
}));

vi.mock('@features/game/presentation/components/GameEventFlowModal', () => ({
  GameEventFlowModal: ({ isOpen }: { isOpen: boolean }) => (isOpen ? <div>이벤트 모달</div> : null),
}));

vi.mock('@features/game/presentation/components/LoanProductsPanel/LoanProductsPanel', () => ({
  LoanProductsPanel: () => <div>대출 패널</div>,
}));

vi.mock('@features/game/presentation/components/CardRecommendPanel/CardRecommendPanel', () => ({
  CardRecommendPanel: () => <div>카드 패널</div>,
}));

vi.mock('@features/game/presentation/components/StockTradingPanel', () => ({
  StockTradingPanel: () => <div>주식 패널</div>,
}));

function renderPage() {
  return render(
    <MemoryRouter initialEntries={[ROUTES.GAME]}>
      <Routes>
        <Route path={ROUTES.GAME} element={<GameMainPage />} />
        <Route path={ROUTES.GAME_START} element={<div>게임 시작 화면</div>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('GameMainPage direct entry', () => {
  beforeEach(() => {
    sessionStorage.clear();
    vi.mocked(container.resolve).mockImplementation((token) => {
      if (token === GetGameSlotsUseCase) {
        return {
          execute: vi.fn().mockResolvedValue([
            {
              slotNumber: 2,
              sessionId: 11,
              status: 'IN_PROGRESS',
              createdAt: '2026-03-01T00:00:00',
            },
          ]),
        };
      }

      if (token === GetGameSessionDetailUseCase) {
        return {
          execute: vi.fn().mockResolvedValue({
            sessionId: 11,
            status: 'IN_PROGRESS',
          }),
        };
      }

      return {
        execute: vi.fn(),
      };
    });

    vi.mocked(useGameGuide).mockReturnValue({
      activeFlowId: null,
      activeStepIndex: 0,
      currentStep: null,
      isOverlayVisible: false,
      isEntryPromptVisible: false,
      launcherFlowId: null,
      isLauncherVisible: false,
      isLastStep: false,
      stepCount: 0,
      toggleFlow: vi.fn(),
      startOrResumeFlow: vi.fn(),
      startFlowAtStep: vi.fn(),
      dismissPrompt: vi.fn(),
      closeGuide: vi.fn(),
      nextStep: vi.fn(),
      previousStep: vi.fn(),
      getTriggerLabel: vi.fn(),
    });

    vi.mocked(useGameTurn).mockImplementation((sessionId) => ({
      turn:
        sessionId === null
          ? null
          : {
              turnNumber: 1,
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
      fetchNewsHistory: vi.fn().mockResolvedValue([]),
      fetchPendingEvents: vi.fn().mockResolvedValue([]),
      fetchTurnActions: vi.fn(),
      resolvePendingEvent: vi.fn().mockResolvedValue(null),
      submitTurnSlots: vi.fn(),
      commitTurn: vi.fn().mockResolvedValue(null),
      dismissResolvedEvent: vi.fn(),
      resetPendingEventFlow: vi.fn(),
      resetScheduleFlow: vi.fn(),
    }));
  });

  it('does not stop at the missing-session creation error on direct entry', async () => {
    renderPage();

    await waitFor(() => {
      expect(container.resolve).toHaveBeenCalledWith(GetGameSlotsUseCase);
    });

    expect(screen.queryByText('세션 생성 정보가 부족합니다.')).not.toBeInTheDocument();
    expect(screen.getByText('현재 상태')).toBeInTheDocument();
    expect(screen.getByText('2,300,000 원')).toBeInTheDocument();
    expect(screen.getByText('이번 달 진행')).toBeInTheDocument();
    expect(screen.getByText('1번째 달')).toBeInTheDocument();
  });

  it('redirects to the game start page when there is no active session to recover', async () => {
    vi.mocked(container.resolve).mockImplementation((token) => {
      if (token === GetGameSlotsUseCase) {
        return {
          execute: vi.fn().mockResolvedValue([
            {
              slotNumber: 1,
              sessionId: null,
              status: 'EMPTY',
              createdAt: null,
            },
          ]),
        };
      }

      if (token === GetGameSessionDetailUseCase) {
        return {
          execute: vi.fn(),
        };
      }

      return {
        execute: vi.fn(),
      };
    });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('게임 시작 화면')).toBeInTheDocument();
    });

    expect(screen.queryByText('세션 생성 정보가 부족합니다.')).not.toBeInTheDocument();
  });
});
