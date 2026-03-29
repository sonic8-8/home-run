import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import { EndingArchivePage } from './EndingArchivePage';
import { useEndingArchivePage } from '@features/ending/presentation/hooks/useEndingArchivePage';

const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

vi.mock('@features/ending/presentation/hooks/useEndingArchivePage', () => ({
  useEndingArchivePage: vi.fn(),
}));

function renderPage() {
  return render(
    <MemoryRouter initialEntries={[ROUTES.GAME_ENDING_ARCHIVE]}>
      <Routes>
        <Route path={ROUTES.GAME_ENDING_ARCHIVE} element={<EndingArchivePage />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('EndingArchivePage', () => {
  beforeEach(() => {
    mockNavigate.mockReset();
    vi.mocked(useEndingArchivePage).mockReturnValue({
      endings: [
        {
          slotNumber: 1,
          sessionId: 10,
          status: 'CLEAR',
          characterName: '하영',
          totalAssets: 320000000,
          createdAt: '2026-01-04T00:00:00',
        },
        {
          slotNumber: 3,
          sessionId: 30,
          status: 'FORECLOSURE',
          characterName: '지훈',
          totalAssets: 85000000,
          createdAt: '2026-03-14T00:00:00',
        },
      ],
      isLoading: false,
      error: null,
      isEmpty: false,
      retryFetch: vi.fn(),
    });
  });

  it('renders archived endings and opens ending detail', () => {
    renderPage();

    expect(screen.getByTestId('ending-archive-page')).toBeInTheDocument();
    expect(screen.getByText('엔딩 저장소')).toBeInTheDocument();
    expect(screen.getByText('하영')).toBeInTheDocument();
    expect(screen.getByText('지훈')).toBeInTheDocument();

    fireEvent.click(screen.getByTestId('ending-archive-card-10'));

    expect(mockNavigate).toHaveBeenCalledWith(ROUTES.GAME_ENDING(10));
  });

  it('shows the empty state when no archived endings exist', () => {
    vi.mocked(useEndingArchivePage).mockReturnValue({
      endings: [],
      isLoading: false,
      error: null,
      isEmpty: true,
      retryFetch: vi.fn(),
    });

    renderPage();

    expect(screen.getByText('아직 저장된 엔딩이 없습니다.')).toBeInTheDocument();
  });
});
