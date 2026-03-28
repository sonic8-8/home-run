import { describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { GameMainPage } from '@features/game/presentation/pages/GameMain/GameMainPage';
import { useGameMain } from '@features/game/presentation/hooks/useGameMain';

vi.mock('@features/game/presentation/hooks/useGameMain', () => ({
  useGameMain: vi.fn(),
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

vi.mock('@features/game/presentation/components/LoanProductsPanel/LoanProductsPanel', () => ({
  LoanProductsPanel: () => <div>대출 패널</div>,
}));

vi.mock('@features/game/presentation/components/CardRecommendPanel/CardRecommendPanel', () => ({
  CardRecommendPanel: () => <div>카드 패널</div>,
}));

describe('GameMainPage', () => {
  it('delegates news open and close actions to useGameMain', () => {
    const openNews = vi.fn();
    const closeNews = vi.fn();

    vi.mocked(useGameMain).mockReturnValue({
      sessionId: 7,
      turn: null,
      news: null,
      currentDate: new Date('2026-03-01T00:00:00'),
      characterType: 'MALE',
      isNewsOpen: true,
      isLoading: false,
      isNewsLoading: false,
      newsError: null,
      openNews,
      closeNews,
      error: null,
      leftView: 'scene',
      setLeftView: vi.fn(),
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
    fireEvent.click(screen.getByRole('button', { name: '뉴스 닫기' }));

    expect(openNews).toHaveBeenCalledTimes(1);
    expect(closeNews).toHaveBeenCalledTimes(1);
  });
});
