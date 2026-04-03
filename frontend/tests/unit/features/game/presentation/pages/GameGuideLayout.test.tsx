import { beforeEach, describe, expect, it } from 'vitest';
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

function MainHarness() {
  const navigate = useNavigate();
  const { startFlowAtStep, toggleFlow } = useGameGuide();

  return (
    <div>
      <button type="button" onClick={() => toggleFlow('main')}>
        main-flow
      </button>
      <button
        type="button"
        data-guide="game-property"
        onClick={() => navigate(ROUTES.REAL_ESTATE)}
      >
        부동산 알아보기
      </button>
      <button
        type="button"
        onClick={() => {
          startFlowAtStep('main', 'main-news-archive');
          navigate(ROUTES.GAME_NEWS(44));
        }}
      >
        news-archive-flow
      </button>
    </div>
  );
}

function PropertyHarness() {
  return <div data-guide="property-map-stage">property-screen</div>;
}

function NewsHarness() {
  const navigate = useNavigate();

  return (
    <div>
      <button type="button" onClick={() => navigate(ROUTES.GAME)}>
        ← 돌아가기
      </button>
      <div data-guide="game-news-archive">news-archive-screen</div>
    </div>
  );
}

function renderGuideFlow(initialEntry: string = ROUTES.GAME_START) {
  return render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <Routes>
        <Route element={<GameGuideLayout />}>
          <Route path={ROUTES.GAME_START} element={<StartHarness />} />
          <Route path={ROUTES.GAME} element={<MainHarness />} />
          <Route path={ROUTES.GAME_SAVE} element={<SaveSlotHarness />} />
          <Route path={ROUTES.GAME_NEWS_PATTERN} element={<NewsHarness />} />
          <Route path={ROUTES.REAL_ESTATE} element={<PropertyHarness />} />
        </Route>
      </Routes>
    </MemoryRouter>,
  );
}

describe('GameGuideLayout', () => {
  beforeEach(() => {
    window.localStorage.clear();
  });

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

  it('releases the current flow when moving to a route owned by another guide', () => {
    renderGuideFlow(ROUTES.GAME);

    fireEvent.click(screen.getByRole('button', { name: 'main-flow' }));

    expect(screen.getByText('뉴스로 이번 달 흐름 읽기')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '부동산 알아보기' }));

    expect(screen.getByText('property-screen')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '가이드 시작' }));

    expect(screen.getByText('지역과 구역부터 좁혀 보기')).toBeInTheDocument();
  });

  it('starts a guide from a specific step on a dynamic route', () => {
    renderGuideFlow(ROUTES.GAME);

    fireEvent.click(screen.getByRole('button', { name: 'news-archive-flow' }));

    expect(screen.getByText('지난 뉴스 흐름 복기')).toBeInTheDocument();
    expect(screen.getByText('news-archive-screen')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '← 돌아가기' }));

    expect(screen.getByText('주식 투자 메뉴 열기')).toBeInTheDocument();
  });
});
