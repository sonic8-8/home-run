import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import { GameBanner } from './GameBanner';

function renderBanner(props: { activeGameSessionId: number | null }) {
  return render(
    <MemoryRouter initialEntries={[ROUTES.HOME]}>
      <Routes>
        <Route
          path={ROUTES.HOME}
          element={
            <GameBanner
              activeGameSessionId={props.activeGameSessionId}
            />
          }
        />
        <Route path={ROUTES.GAME} element={<div>게임 메인 화면</div>} />
        <Route path={ROUTES.GAME_START} element={<div>게임 시작 화면</div>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('GameBanner', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows a resume CTA when an in-progress session exists', async () => {
    renderBanner({ activeGameSessionId: 31 });

    await waitFor(() => {
      expect(screen.getByRole('button', { name: '현재 세션 이어하기' })).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: '현재 세션 이어하기' }));

    await waitFor(() => {
      expect(screen.getByText('게임 메인 화면')).toBeInTheDocument();
    });
  });

  it('falls back to the game start CTA when there is no in-progress session', async () => {
    renderBanner({ activeGameSessionId: null });

    await waitFor(() => {
      expect(screen.getByRole('button', { name: '게임 시작하기' })).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: '게임 시작하기' }));

    await waitFor(() => {
      expect(screen.getByText('게임 시작 화면')).toBeInTheDocument();
    });
  });
});
