import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import { EndingPage } from './EndingPage';
import { useEndingPage } from '@features/ending/presentation/hooks/useEndingPage';
import type { EndingType } from '@features/ending/domain/entities/EndingReport';

vi.mock('@features/ending/presentation/hooks/useEndingPage', () => ({
  useEndingPage: vi.fn(),
}));

function renderPage() {
  return render(
    <MemoryRouter initialEntries={[ROUTES.GAME_ENDING(10)]}>
      <Routes>
        <Route path={ROUTES.GAME_ENDING_PATTERN} element={<EndingPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

function buildReport(endingType: EndingType, title: string) {
  return {
    characterType: 'FEMALE' as const,
    endingType,
    grade: 'S',
    title,
    totalAssets: 350000000,
    totalIncome: 220000000,
    totalExpense: 90000000,
    netProfit: 130000000,
    spendingPattern: {
      topCategory: '주거',
      topCategoryRatio: 0.41,
    },
    achievements: [],
    newsHistories: [
      {
        turnNumber: 4,
        newsId: 'NEWS-1',
        headline: '금리 인하',
        publishedDate: new Date('2026-03-01'),
      },
      {
        turnNumber: 7,
        newsId: 'NEWS-2',
        headline: '부동산 규제 완화',
        publishedDate: new Date('2026-06-01'),
      },
    ],
    eventHistories: [
      {
        turnNumber: 6,
        gameEventId: 11,
        selectedChoiceCode: 'A',
        resultSummary: '연봉 협상 성공',
        resolvedAt: new Date('2026-05-01T00:00:00'),
      },
      {
        turnNumber: 9,
        gameEventId: 17,
        selectedChoiceCode: 'B',
        resultSummary: '이직 제안을 거절했다',
        resolvedAt: new Date('2026-08-01T00:00:00'),
      },
    ],
    housingHistories: [
      {
        turnNumber: 8,
        summary: '자가 구매',
        beforeState: { housingType: 'JEONSE_APT' as const, propertyId: 20 },
        afterState: { housingType: 'OWNED_APT' as const, propertyId: 30 },
      },
      {
        turnNumber: 5,
        summary: '전세 입주',
        beforeState: { housingType: 'VILLA' as const, propertyId: 10 },
        afterState: { housingType: 'JEONSE_APT' as const, propertyId: 20 },
      },
    ],
    housingSnapshot: {
      currentHousingType: 'OWNED_APT' as const,
      currentPropertyId: 30,
      targetPropertyId: 30,
    },
  };
}

describe('EndingPage', () => {
  beforeEach(() => {
    vi.mocked(useEndingPage).mockReturnValue({
      report: buildReport('CLEAR', '내 집 마련 성공'),
      timeline: [
        {
          turnNumber: 1,
          date: new Date('2026-01-01'),
          cash: 1000000,
          netAssets: 2000000,
          totalAssets: 3000000,
          stockValue: 0,
          loanBalance: 0,
          salary: 2500000,
        },
        {
          turnNumber: 2,
          date: new Date('2026-02-01'),
          cash: 2000000,
          netAssets: 2400000,
          totalAssets: 3400000,
          stockValue: 100000,
          loanBalance: 0,
          salary: 2500000,
        },
      ],
      isLoading: false,
      error: null,
      isNotReady: false,
      hasValidSessionId: true,
    });
  });

  it('reveals the ending report after clicking the scene CTA', () => {
    renderPage();

    expect(screen.getByTestId('ending-scene')).toBeInTheDocument();
    expect(screen.getByText('내 집 마련 성공')).toBeInTheDocument();
    expect(screen.getByText('>> 결과 보기')).toBeInTheDocument();
    expect(screen.queryByTestId('ending-summary')).not.toBeInTheDocument();

    fireEvent.click(screen.getByTestId('ending-reveal-report'));

    expect(screen.getByTestId('ending-summary')).toBeInTheDocument();
    expect(screen.getByTestId('ending-timeline')).toBeInTheDocument();
    expect(screen.getByText('총 2개의 월별 로그가 준비되었습니다.')).toBeInTheDocument();
    expect(screen.getByTestId('ending-histories')).toBeInTheDocument();
    expect(screen.getByTestId('ending-news-history')).toBeInTheDocument();
    expect(screen.getByTestId('ending-event-history')).toBeInTheDocument();
    expect(screen.getByTestId('ending-housing-history')).toBeInTheDocument();
    expect(screen.getByText('금리 인하')).toBeInTheDocument();
    expect(screen.getByText('부동산 규제 완화')).toBeInTheDocument();
    expect(screen.getByText('연봉 협상 성공')).toBeInTheDocument();
    expect(screen.getByText('이직 제안을 거절했다')).toBeInTheDocument();
    expect(screen.getByText('전세 입주')).toBeInTheDocument();
    expect(screen.getByTestId('ending-housing-history')).toHaveTextContent('현재 주거');
    expect(screen.getByTestId('ending-housing-history')).toHaveTextContent('자가');
    expect(screen.getByTestId('ending-housing-history')).toHaveTextContent('목표 주거 달성');
    expect(screen.getByTestId('ending-housing-history')).toHaveTextContent('달성');
  });

  it.each([
    ['CLEAR', '내 집 마련 성공'],
    ['BANKRUPT', '파산'],
    ['TIMEOUT', '기한 초과'],
    ['FORECLOSURE', '차압'],
  ] satisfies [EndingType, string][])(
    'renders the ending scene for %s',
    (endingType, title) => {
      vi.mocked(useEndingPage).mockReturnValue({
        report: buildReport(endingType, title),
        timeline: [],
        isLoading: false,
        error: null,
        isNotReady: false,
        hasValidSessionId: true,
      });

      renderPage();

      expect(screen.getByTestId('ending-scene')).toHaveTextContent(title);
      expect(screen.getByTestId('ending-reveal-report')).toBeInTheDocument();
    },
  );

  it('renders the not-ready state when the ending report is not available yet', () => {
    vi.mocked(useEndingPage).mockReturnValue({
      report: null,
      timeline: [],
      isLoading: false,
      error: null,
      isNotReady: true,
      hasValidSessionId: true,
    });

    renderPage();

    expect(screen.getByTestId('ending-not-ready')).toBeInTheDocument();
    expect(screen.getByText('엔딩 리포트 준비 중')).toBeInTheDocument();
  });
});
