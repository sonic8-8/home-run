import { describe, expect, it } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useNavigate } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import { GameGuideLayout } from '@features/game/presentation/components/GameGuideLayout';
import { useGameGuide } from '@features/game/presentation/hooks/useGameGuide';

function StartHarness() {
  const navigate = useNavigate();
  const { toggleFlow } = useGameGuide();

  return (
    <div>
      <button type="button" onClick={() => toggleFlow('start')}>
        start-flow
      </button>
      <button
        type="button"
        data-guide="game-start-new"
        onClick={() => navigate(ROUTES.GAME_SAVE)}
      >
        새로하기
      </button>
    </div>
  );
}

function SaveSlotHarness() {
  return <div data-guide="game-save-slots">save-slot-screen</div>;
}

function renderGuideFlow() {
  return render(
    <MemoryRouter initialEntries={[ROUTES.GAME_START]}>
      <Routes>
        <Route element={<GameGuideLayout />}>
          <Route path={ROUTES.GAME_START} element={<StartHarness />} />
          <Route path={ROUTES.GAME_SAVE} element={<SaveSlotHarness />} />
        </Route>
      </Routes>
    </MemoryRouter>,
  );
}

describe('GameGuideLayout', () => {
  it('continues the onboarding guide after the route changes', () => {
    renderGuideFlow();

    fireEvent.click(screen.getByRole('button', { name: 'start-flow' }));

    expect(screen.getByText('새로하기부터 시작')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '새로하기' }));

    expect(screen.getByText('저장 슬롯 고르기')).toBeInTheDocument();
    expect(screen.getByText('save-slot-screen')).toBeInTheDocument();
  });

  it('shows a resume launcher on guided routes after the overlay is closed', () => {
    renderGuideFlow();

    fireEvent.click(screen.getByRole('button', { name: 'start-flow' }));
    fireEvent.click(screen.getByRole('button', { name: '새로하기' }));

    expect(screen.getByText('저장 슬롯 고르기')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '닫기' }));

    expect(screen.getByRole('button', { name: '가이드 이어보기' })).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '가이드 이어보기' }));

    expect(screen.getByText('저장 슬롯 고르기')).toBeInTheDocument();
  });
});
