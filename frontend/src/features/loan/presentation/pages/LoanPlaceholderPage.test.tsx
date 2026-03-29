import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import { LoanPlaceholderPage } from './LoanPlaceholderPage';

const mockNavigate = vi.fn();
const mockResolve = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

vi.mock('@core/di/container', () => ({
  container: {
    resolve: (...args: unknown[]) => mockResolve(...args),
  },
}));

function renderPage() {
  return render(
    <MemoryRouter initialEntries={[ROUTES.LOAN]}>
      <Routes>
        <Route path={ROUTES.LOAN} element={<LoanPlaceholderPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('LoanPlaceholderPage', () => {
  beforeEach(() => {
    mockNavigate.mockReset();
    mockResolve.mockReset();
  });

  it('guides the player back to the game loan panel for the latest active session', async () => {
    mockResolve.mockReturnValue({
      execute: vi.fn().mockResolvedValue([
        {
          slotNumber: 1,
          sessionId: 4,
          status: 'IN_PROGRESS',
          createdAt: '2026-03-01T00:00:00',
        },
        {
          slotNumber: 2,
          sessionId: 9,
          status: 'IN_PROGRESS',
          createdAt: '2026-03-20T00:00:00',
        },
      ]),
    });

    renderPage();

    await waitFor(() => {
      expect(screen.getByTestId('loan-active-session')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: '게임으로 돌아가기' }));

    expect(mockNavigate).toHaveBeenCalledWith(ROUTES.GAME, {
      state: {
        sessionId: 9,
        openLoan: true,
      },
    });
  });

  it('shows the no-session state and navigates to game start', async () => {
    mockResolve.mockReturnValue({
      execute: vi.fn().mockResolvedValue([
        {
          slotNumber: 1,
          sessionId: null,
          status: 'EMPTY',
        },
      ]),
    });

    renderPage();

    await waitFor(() => {
      expect(screen.getByTestId('loan-no-session')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: '게임 시작하기' }));

    expect(mockNavigate).toHaveBeenCalledWith(ROUTES.GAME_START);
  });
});
