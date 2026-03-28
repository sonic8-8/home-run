import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import { NewsPage } from './NewsPage';
import { useGameNewsPage } from '@features/game/presentation/hooks/useGameNewsPage';

vi.mock('@features/game/presentation/hooks/useGameNewsPage', () => ({
  useGameNewsPage: vi.fn(),
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
      isNewsLoading: false,
      newsError: null,
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
      isNewsLoading: false,
      newsError: null,
      hasValidSessionId: true,
    });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('홈런 경제 신문')).toBeInTheDocument();
    });
    expect(screen.getByText('홈런 경제 신문')).toBeInTheDocument();
    expect(screen.getByText('2026-01-01 · 12번째 달')).toBeInTheDocument();
    expect(screen.getByText('부동산 시장 과열 경고')).toBeInTheDocument();
    fireEvent.click(screen.getByText('부동산 시장 과열 경고'));
    expect(screen.getByText('시장 과열 신호가 확인됐다.')).toBeInTheDocument();
  });
});
