import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import { RealEstatePage } from './RealEstatePage';
import { useLoan } from '@features/loan/presentation/hooks/useLoan';
import { useGameGuide } from '@features/game/presentation/hooks/useGameGuide';

vi.mock('@features/loan/presentation/hooks/useLoan', () => ({
  useLoan: vi.fn(),
}));

vi.mock('@features/game/presentation/hooks/useGameGuide', () => ({
  useGameGuide: vi.fn(),
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
        <Route path={ROUTES.GAME} element={<div>게임 메인 화면</div>} />
        <Route path={path} element={<RealEstatePage />} />
      </Routes>
    </MemoryRouter>,
  );
}

function renderPageWithEntry(entry: { pathname: string; state?: unknown }) {
  return render(
    <MemoryRouter initialEntries={[entry]}>
      <Routes>
        <Route path={ROUTES.GAME} element={<div>게임 메인 화면</div>} />
        <Route path={ROUTES.REAL_ESTATE_LOAN_APPLY} element={<RealEstatePage />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('RealEstatePage direct entry', () => {
  beforeEach(() => {
    vi.mocked(useGameGuide).mockReturnValue({
      activeFlowId: null,
      activeStepIndex: 0,
      currentStep: null,
      isOverlayVisible: false,
      isEntryPromptVisible: false,
      launcherFlowId: null,
      isLauncherVisible: false,
      isLastStep: false,
      stepCount: 0,
      toggleFlow: vi.fn(),
      startOrResumeFlow: vi.fn(),
      startFlowAtStep: vi.fn(),
      dismissPrompt: vi.fn(),
      closeGuide: vi.fn(),
      nextStep: vi.fn(),
      previousStep: vi.fn(),
      getTriggerLabel: vi.fn(),
    });

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

  it('returns to the explicit game route when the loan review flow is closed', async () => {
    const apply = vi.fn().mockResolvedValue({
      applicationId: 17,
      status: 'APPROVED',
      requestInfo: {
        applicationDate: '2026-04-06 10:00:00',
      },
      result: {
        maxLoanAmount: 650_000_000,
      },
    });

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
      apply,
      confirm: vi.fn(),
      repay: vi.fn(),
    });

    renderPageWithEntry({
      pathname: ROUTES.REAL_ESTATE_LOAN_APPLY,
      state: {
        sessionId: 7,
        productId: 'loan-1',
        preSelectedPropertyId: 'apt-77',
        preSelectedPropertyName: '테스트 아파트',
        preSelectedPropertyPrice: 1_200_000_000,
        returnTo: {
          pathname: ROUTES.GAME,
          state: {
            sessionId: 7,
            openLoan: true,
          },
        },
      },
    });

    await waitFor(() => {
      expect(screen.getByRole('button', { name: '확인 (닫기)' })).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: '확인 (닫기)' }));

    await waitFor(() => {
      expect(screen.getByText('게임 메인 화면')).toBeInTheDocument();
    });
  });
});
