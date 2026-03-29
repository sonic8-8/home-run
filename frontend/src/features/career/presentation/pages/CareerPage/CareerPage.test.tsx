import { describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { CareerPage } from './CareerPage';
import { useCareerPage } from '../../hooks/useCareerPage';

vi.mock('../../hooks/useCareerPage', () => ({
  useCareerPage: vi.fn(),
}));

vi.mock('@shared/components/PageSpinner', () => ({
  PageSpinner: () => <div>로딩 중</div>,
}));

describe('CareerPage', () => {
  it('renders the active session summary and career offers', () => {
    vi.mocked(useCareerPage).mockReturnValue({
      activeSession: {
        sessionId: 42,
        slotNumber: 2,
        characterName: '지민',
        jobType: 'STARTUP',
        currentTurn: 8,
      },
      jobOfferList: {
        offers: [
          {
            offerId: 'offer-1',
            jobType: 'LARGE_BIZ',
            companyName: '홈런전자',
            currentSalary: 48000000,
            offeredSalary: 56000000,
            probationTurns: 2,
          },
        ],
        offerChanceBonusRate: 10,
        meetFriendBonusApplied: true,
      },
      currentSalary: 48000000,
      salaryNegotiationResult: null,
      transferResult: null,
      isSessionLoading: false,
      isOffersLoading: false,
      isNegotiating: false,
      transferringOfferId: null,
      pageError: null,
      offerError: null,
      actionError: null,
      refresh: vi.fn(),
      negotiateSalary: vi.fn(),
      transferJob: vi.fn(),
    });

    render(
      <MemoryRouter>
        <CareerPage />
      </MemoryRouter>,
    );

    expect(screen.getByRole('heading', { name: '커리어 협상과 이직 관리' })).toBeInTheDocument();
    expect(screen.getByText('지민')).toBeInTheDocument();
    expect(screen.getByText('홈런전자')).toBeInTheDocument();
    expect(screen.getByText('제안 보너스 10%')).toBeInTheDocument();
  });

  it('delegates negotiation and transfer actions to the hook', () => {
    const negotiateSalary = vi.fn();
    const transferJob = vi.fn();

    vi.mocked(useCareerPage).mockReturnValue({
      activeSession: {
        sessionId: 19,
        slotNumber: 1,
        characterName: '민수',
        jobType: 'SMALL_BIZ',
        currentTurn: 5,
      },
      jobOfferList: {
        offers: [
          {
            offerId: 'offer-2',
            jobType: 'MID_BIZ',
            companyName: '홈런테크',
            currentSalary: 40000000,
            offeredSalary: 47000000,
            probationTurns: null,
          },
        ],
        offerChanceBonusRate: 0,
        meetFriendBonusApplied: false,
      },
      currentSalary: 40000000,
      salaryNegotiationResult: null,
      transferResult: null,
      isSessionLoading: false,
      isOffersLoading: false,
      isNegotiating: false,
      transferringOfferId: null,
      pageError: null,
      offerError: null,
      actionError: null,
      refresh: vi.fn(),
      negotiateSalary,
      transferJob,
    });

    render(
      <MemoryRouter>
        <CareerPage />
      </MemoryRouter>,
    );

    fireEvent.click(screen.getByRole('button', { name: '연봉 협상 진행' }));
    fireEvent.click(screen.getByRole('button', { name: '이직 수락' }));

    expect(negotiateSalary).toHaveBeenCalledTimes(1);
    expect(transferJob).toHaveBeenCalledWith('offer-2');
  });

  it('shows the empty state when no active session exists', () => {
    vi.mocked(useCareerPage).mockReturnValue({
      activeSession: null,
      jobOfferList: null,
      currentSalary: null,
      salaryNegotiationResult: null,
      transferResult: null,
      isSessionLoading: false,
      isOffersLoading: false,
      isNegotiating: false,
      transferringOfferId: null,
      pageError: null,
      offerError: null,
      actionError: null,
      refresh: vi.fn(),
      negotiateSalary: vi.fn(),
      transferJob: vi.fn(),
    });

    render(
      <MemoryRouter>
        <CareerPage />
      </MemoryRouter>,
    );

    expect(screen.getByTestId('career-empty-state')).toBeInTheDocument();
    expect(screen.getByText('진행 중인 게임 세션이 없습니다.')).toBeInTheDocument();
  });
});
