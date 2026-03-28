import { StrictMode } from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { ROUTES } from '@app/routes';
import type { GameSessionCreation } from '@features/game/domain/entities/GameSessionCreation';
import { useGameMain } from '@features/game/presentation/hooks/useGameMain';
import { useCreateGameSession } from '@features/game/presentation/hooks/useCreateGameSession';
import { useGameTurn } from '@features/game/presentation/hooks/useGameTurn';

vi.mock('@features/game/presentation/hooks/useCreateGameSession', () => ({
  useCreateGameSession: vi.fn(),
}));

vi.mock('@features/game/presentation/hooks/useGameTurn', () => ({
  useGameTurn: vi.fn(),
}));

function createDeferred<T>() {
  let resolve: (value: T) => void = () => {
    throw new Error('Deferred promise resolver was not initialized.');
  };

  const promise = new Promise<T>((nextResolve) => {
    resolve = nextResolve;
  });

  return { promise, resolve };
}

function GameMainProbe() {
  const { sessionId, isLoading, error } = useGameMain();
  const location = useLocation();

  return (
    <div>
      <span data-testid="session-id">{sessionId ?? 'null'}</span>
      <span data-testid="loading">{String(isLoading)}</span>
      <span data-testid="error">{error ?? 'null'}</span>
      <pre data-testid="route-state">{JSON.stringify(location.state ?? null)}</pre>
    </div>
  );
}

function renderPage() {
  return render(
    <StrictMode>
      <MemoryRouter
        initialEntries={[
          {
            pathname: ROUTES.GAME,
            state: {
              slotNumber: 3,
              characterType: 'FEMALE',
              characterName: '테스터',
              regionCode: '11',
              districtCode: '11680',
              targetPropertyId: 101,
              useMyData: true,
            },
          },
        ]}
      >
        <Routes>
          <Route path={ROUTES.GAME} element={<GameMainProbe />} />
        </Routes>
      </MemoryRouter>
    </StrictMode>,
  );
}

describe('useGameMain', () => {
  it('reuses the in-flight create-session promise in StrictMode and then loads the turn', async () => {
    const fetchTurnSessionIds: number[] = [];
    let latestSessionId: number | null = null;
    const createSessionDeferred = createDeferred<GameSessionCreation>();

    const createSession = vi.fn(() => createSessionDeferred.promise);
    const fetchTurn = vi.fn(async () => {
      if (latestSessionId !== null) {
        fetchTurnSessionIds.push(latestSessionId);
      }
      return null;
    });
    const fetchLatestNews = vi.fn(async () => null);

    vi.mocked(useCreateGameSession).mockReturnValue({
      createSession,
      isSubmitting: false,
      error: null,
    });
    vi.mocked(useGameTurn).mockImplementation((sessionId) => {
      latestSessionId = sessionId;

      return {
        turn: null,
        news: null,
        isTurnLoading: false,
        turnError: null,
        isNewsLoading: false,
        newsError: null,
        fetchTurn,
        fetchLatestNews,
      };
    });

    renderPage();

    expect(createSession).toHaveBeenCalledTimes(1);

    createSessionDeferred.resolve({
      sessionId: 31,
      slotNumber: 3,
      status: 'IN_PROGRESS',
      currentTurn: 1,
      dataSourceType: 'MY_DATA',
    });

    await waitFor(() => {
      expect(fetchTurn).toHaveBeenCalled();
    });
    expect(fetchTurnSessionIds).toContain(31);

    await waitFor(() => {
      expect(screen.getByTestId('session-id')).toHaveTextContent('31');
    });
    expect(screen.getByTestId('route-state')).toHaveTextContent('"sessionId":31');
    expect(screen.getByTestId('loading')).toHaveTextContent('false');
    expect(screen.getByTestId('error')).toHaveTextContent('null');
  });
});
