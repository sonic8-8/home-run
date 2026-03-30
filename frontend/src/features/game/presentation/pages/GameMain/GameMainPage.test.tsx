import { describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { GameMainPage } from '@features/game/presentation/pages/GameMain/GameMainPage';
import { useGameMain } from '@features/game/presentation/hooks/useGameMain';

const mockedGuideToggle = vi.fn();

vi.mock('@features/game/presentation/hooks/useGameMain', () => ({
  useGameMain: vi.fn(),
}));

vi.mock('@features/game/presentation/hooks/useGameGuide', () => ({
  useGameGuide: () => ({
    toggleFlow: mockedGuideToggle,
    getTriggerLabel: () => '게임 가이드',
  }),
}));

vi.mock('@features/game/presentation/components/NewsEventModal/NewsEventModal', () => ({
  NewsEventModal: ({
    isOpen,
    onClose,
  }: {
    isOpen: boolean;
    onClose: () => void;
  }) => (
    isOpen ? <button onClick={onClose}>뉴스 닫기</button> : null
  ),
}));

vi.mock('@features/game/presentation/components/MonthlyActivityModal/MonthlyActivityModal', () => ({
  MonthlyActivityModal: ({
    isOpen,
    onClose,
  }: {
    isOpen: boolean;
    onClose: () => void;
  }) => (
    isOpen ? <button onClick={onClose}>월 활동 닫기</button> : null
  ),
}));

vi.mock('@features/game/presentation/components/GameEventFlowModal', () => ({
  GameEventFlowModal: ({ isOpen }: { isOpen: boolean }) => (
    isOpen ? <div>이벤트 모달</div> : null
  ),
}));

vi.mock('@features/game/presentation/components/LoanProductsPanel/LoanProductsPanel', () => ({
  LoanProductsPanel: () => <div>대출 패널</div>,
}));

vi.mock('@features/game/presentation/components/CardRecommendPanel/CardRecommendPanel', () => ({
  CardRecommendPanel: () => <div>카드 패널</div>,
}));

vi.mock('@features/game/presentation/components/GameStatusPanel', () => ({
  GameStatusPanel: () => <div>자산 패널</div>,
}));

vi.mock('@features/game/presentation/components/StockTradingPanel', () => ({
  StockTradingPanel: () => <div>주식 패널</div>,
}));

describe('GameMainPage', () => {
  it('delegates news open and close actions to useGameMain', () => {
    mockedGuideToggle.mockReset();
    const openNews = vi.fn();
    const closeNews = vi.fn();
    const openMonthlyActivity = vi.fn();
    const closeMonthlyActivity = vi.fn();

    vi.mocked(useGameMain).mockReturnValue({
      sessionId: 7,
      turn: {
        turnNumber: 7,
        month: 3,
        currentDate: new Date('2026-03-01T00:00:00'),
        economicCycle: {
          phase: 'BOOM',
          description: '경기 호황기',
        },
      },
      news: null,
      currentPendingEvent: null,
      resolvedEvent: null,
      hasMorePendingEvents: false,
      turnActions: null,
      turnPreview: null,
      turnCommitResult: null,
      currentDate: new Date('2026-03-01T00:00:00'),
      characterType: 'MALE',
      isNewsOpen: true,
      isMonthlyActivityOpen: true,
      isGameEventOpen: false,
      isLoading: false,
      isNewsLoading: false,
      newsError: null,
      isActionsLoading: false,
      isSlotSubmitting: false,
      isTurnCommitting: false,
      isPendingEventsLoading: false,
      isEventResolving: false,
      scheduleError: null,
      eventError: null,
      openNews,
      closeNews,
      openMonthlyActivity,
      closeMonthlyActivity,
      closeGameEvent: vi.fn(),
      submitTurnSlots: vi.fn(),
      commitTurn: vi.fn(),
      confirmCommitResult: vi.fn(),
      resolveGameEvent: vi.fn(),
      advanceGameEvent: vi.fn(),
      resetScheduleFlow: vi.fn(),
      error: null,
      leftView: 'scene',
      setLeftView: vi.fn(),
      confirmedLoan: undefined,
      preSelectedPropertyId: undefined,
      preSelectedPropertyName: undefined,
      preSelectedPropertyPrice: undefined,
    });

    render(
      <MemoryRouter>
        <GameMainPage />
      </MemoryRouter>,
    );

    fireEvent.click(screen.getByRole('button', { name: '이달의 뉴스' }));
    fireEvent.click(screen.getByRole('button', { name: '게임 가이드' }));
    fireEvent.click(screen.getByRole('button', { name: '뉴스 닫기' }));
    fireEvent.click(screen.getByRole('button', { name: '이번 달 활동 진행' }));
    fireEvent.click(screen.getByRole('button', { name: '월 활동 닫기' }));

    expect(openNews).toHaveBeenCalledTimes(1);
    expect(mockedGuideToggle).toHaveBeenCalledWith('main');
    expect(closeNews).toHaveBeenCalledTimes(1);
    expect(openMonthlyActivity).toHaveBeenCalledTimes(1);
    expect(closeMonthlyActivity).toHaveBeenCalledTimes(1);
  });

  it('opens the stock panel from the in-game menu', () => {
    mockedGuideToggle.mockReset();
    const setLeftView = vi.fn();

    vi.mocked(useGameMain).mockReturnValue({
      sessionId: 7,
      turn: {
        turnNumber: 7,
        month: 3,
        currentDate: new Date('2026-03-01T00:00:00'),
        economicCycle: {
          phase: 'BOOM',
          description: '경기 호황기',
        },
      },
      news: null,
      currentPendingEvent: null,
      resolvedEvent: null,
      hasMorePendingEvents: false,
      turnActions: null,
      turnPreview: null,
      turnCommitResult: null,
      currentDate: new Date('2026-03-01T00:00:00'),
      characterType: 'MALE',
      isNewsOpen: false,
      isMonthlyActivityOpen: false,
      isGameEventOpen: false,
      isLoading: false,
      isNewsLoading: false,
      newsError: null,
      isActionsLoading: false,
      isSlotSubmitting: false,
      isTurnCommitting: false,
      isPendingEventsLoading: false,
      isEventResolving: false,
      scheduleError: null,
      eventError: null,
      openNews: vi.fn(),
      closeNews: vi.fn(),
      openMonthlyActivity: vi.fn(),
      closeMonthlyActivity: vi.fn(),
      closeGameEvent: vi.fn(),
      submitTurnSlots: vi.fn(),
      commitTurn: vi.fn(),
      confirmCommitResult: vi.fn(),
      resolveGameEvent: vi.fn(),
      advanceGameEvent: vi.fn(),
      resetScheduleFlow: vi.fn(),
      error: null,
      leftView: 'scene',
      setLeftView,
      confirmedLoan: undefined,
      preSelectedPropertyId: undefined,
      preSelectedPropertyName: undefined,
      preSelectedPropertyPrice: undefined,
    });

    render(
      <MemoryRouter>
        <GameMainPage />
      </MemoryRouter>,
    );

    fireEvent.click(screen.getByRole('button', { name: '주식 투자' }));

    expect(setLeftView).toHaveBeenCalledWith('stock');
  });

  it('opens the status panel from the in-game menu', () => {
    mockedGuideToggle.mockReset();
    const setLeftView = vi.fn();

    vi.mocked(useGameMain).mockReturnValue({
      sessionId: 7,
      turn: {
        turnNumber: 7,
        month: 3,
        currentDate: new Date('2026-03-01T00:00:00'),
        economicCycle: {
          phase: 'BOOM',
          description: '경기 호황기',
        },
      },
      news: null,
      currentPendingEvent: null,
      resolvedEvent: null,
      hasMorePendingEvents: false,
      turnActions: null,
      turnPreview: null,
      turnCommitResult: null,
      currentDate: new Date('2026-03-01T00:00:00'),
      characterType: 'MALE',
      isNewsOpen: false,
      isMonthlyActivityOpen: false,
      isGameEventOpen: false,
      isLoading: false,
      isNewsLoading: false,
      newsError: null,
      isActionsLoading: false,
      isSlotSubmitting: false,
      isTurnCommitting: false,
      isPendingEventsLoading: false,
      isEventResolving: false,
      scheduleError: null,
      eventError: null,
      openNews: vi.fn(),
      closeNews: vi.fn(),
      openMonthlyActivity: vi.fn(),
      closeMonthlyActivity: vi.fn(),
      closeGameEvent: vi.fn(),
      submitTurnSlots: vi.fn(),
      commitTurn: vi.fn(),
      confirmCommitResult: vi.fn(),
      resolveGameEvent: vi.fn(),
      advanceGameEvent: vi.fn(),
      resetScheduleFlow: vi.fn(),
      error: null,
      leftView: 'scene',
      setLeftView,
      confirmedLoan: undefined,
      preSelectedPropertyId: undefined,
      preSelectedPropertyName: undefined,
      preSelectedPropertyPrice: undefined,
    });

    render(
      <MemoryRouter>
        <GameMainPage />
      </MemoryRouter>,
    );

    fireEvent.click(screen.getByRole('button', { name: '자산/스탯 보기' }));

    expect(setLeftView).toHaveBeenCalledWith('status');
  });
});
