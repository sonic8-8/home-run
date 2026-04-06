import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import { RealEstatePage } from './RealEstatePage';
import { useLoan } from '@features/loan/presentation/hooks/useLoan';

vi.mock('@features/loan/presentation/hooks/useLoan', () => ({
  useLoan: vi.fn(),
}));

vi.mock('../../components/KoreaMap/KoreaMap', () => ({
  KoreaMap: () => <div>Real Estate Map</div>,
}));

function renderPage() {
  return render(
    <MemoryRouter initialEntries={[ROUTES.REAL_ESTATE]}>
      <Routes>
        <Route path={ROUTES.REAL_ESTATE} element={<RealEstatePage />} />
      </Routes>
    </MemoryRouter>,
  );
}

function renderPageWithRoute(path: string) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path={path} element={<RealEstatePage />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('RealEstatePage direct entry', () => {
  beforeEach(() => {
    vi.mocked(useLoan).mockReturnValue({
      productsPage: null,
      selectedProduct: null,
      calculation: null,
      application: null,
      confirmedLoan: null,
      loading: false,
      error: null,
      fetchProducts: vi.fn(),
      fetchProductDetail: vi.fn(),
      calculate: vi.fn(),
      apply: vi.fn(),
      confirm: vi.fn(),
      repay: vi.fn(),
    });
  });

  it('does not stop at the missing-session error in browse mode', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.queryByText('세션 정보를 확인하지 못했습니다.')).not.toBeInTheDocument();
    });

    expect(screen.getByText('Real Estate Map')).toBeInTheDocument();
  });

  it('shows an explicit guard when the new-game property route is opened without game setup state', async () => {
    renderPageWithRoute('/property/new-game');

    await waitFor(() => {
      expect(screen.getByText('새 게임 시작 정보를 확인하지 못했습니다.')).toBeInTheDocument();
    });
  });

  it('shows the loan-apply guard when the loan property route is opened without session state', async () => {
    renderPageWithRoute('/property/loan-apply');

    await waitFor(() => {
      expect(screen.getByText('세션 정보를 확인하지 못했습니다.')).toBeInTheDocument();
    });
  });
});
