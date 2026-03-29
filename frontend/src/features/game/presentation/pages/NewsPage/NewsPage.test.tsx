import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import { NewsPage } from './NewsPage';
import { useGameGuide } from '@features/game/presentation/hooks/useGameGuide';
import { useGameNewsPage } from '@features/game/presentation/hooks/useGameNewsPage';

vi.mock('@features/game/presentation/hooks/useGameNewsPage', () => ({
  useGameNewsPage: vi.fn(),
}));

vi.mock('@features/game/presentation/hooks/useGameGuide', () => ({
  useGameGuide: vi.fn(),
}));

function renderPage() {
  return render(
    <MemoryRouter
      initialEntries={[
        {
          pathname: ROUTES.GAME_NEWS(44),
        },
      ]}
    >
      <Routes>
        <Route path={ROUTES.GAME_NEWS_PATTERN} element={<NewsPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('NewsPage', () => {
  beforeEach(() => {
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

    vi.mocked(useGameNewsPage).mockReturnValue({
      news: {
        turnNumber: 12,
        currentDate: new Date('2026-01-01T00:00:00'),
        news: [
          {
            newsId: 'news-1',
            headline: '부동산 시장 과열 경고',
            content: '시장 과열 신호가 확인됐다.',
            sourceName: '영남일보',
            publishedDate: new Date('2026-01-01T00:00:00'),
            economicCycleType: 'BOOM_TO_CRISIS',
          },
        ],
      },
      newsHistory: [
        {
          turnNumber: 11,
          newsId: 'NEWS-011',
          headline: '채용 한파 심화',
          publishedDate: new Date('2025-12-01T00:00:00'),
        },
      ],
      isNewsLoading: false,
      newsError: null,
      isNewsHistoryLoading: false,
      newsHistoryError: null,
      hasValidSessionId: true,
    });
  });

  it('loads and renders the latest news for the session route param', async () => {
    vi.mocked(useGameNewsPage).mockReturnValue({
      news: {
        turnNumber: 12,
        currentDate: new Date('2026-01-01T00:00:00'),
        news: [
          {
            newsId: 'news-1',
            headline: '부동산 시장 과열 경고',
            content: '시장 과열 신호가 확인됐다.',
            sourceName: '영남일보',
            publishedDate: new Date('2026-01-01T00:00:00'),
            economicCycleType: 'BOOM_TO_CRISIS',
          },
        ],
      },
      newsHistory: [
        {
          turnNumber: 11,
          newsId: 'NEWS-011',
          headline: '채용 한파 심화',
          publishedDate: new Date('2025-12-01T00:00:00'),
        },
      ],
      isNewsLoading: false,
      newsError: null,
      isNewsHistoryLoading: false,
      newsHistoryError: null,
      hasValidSessionId: true,
    });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('홈런 경제 신문')).toBeInTheDocument();
    });
    expect(screen.getByText('홈런 경제 신문')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '뉴스 가이드' })).toBeInTheDocument();
    expect(screen.getByText('2026-01-01 · 12번째 달')).toBeInTheDocument();
    expect(screen.getByText('부동산 시장 과열 경고')).toBeInTheDocument();
    expect(screen.getByText('지난 턴 헤드라인')).toBeInTheDocument();
    expect(screen.getByText('채용 한파 심화')).toBeInTheDocument();
    fireEvent.click(screen.getByText('부동산 시장 과열 경고'));
    expect(screen.getByText('시장 과열 신호가 확인됐다.')).toBeInTheDocument();
  });
});
