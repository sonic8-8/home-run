import type { ReactNode } from 'react';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { LoanProductsPanel } from './LoanProductsPanel';

const mockFetchProducts = vi.fn();
const mockRepay = vi.fn();
const mockWriteActiveLoan = vi.fn();

vi.mock('@features/loan/presentation/hooks/useLoan', () => ({
  useLoan: () => ({
    productsPage: { content: [] },
    loading: false,
    error: null,
    fetchProducts: mockFetchProducts,
    repay: mockRepay,
  }),
}));

vi.mock('@features/loan/presentation/activeLoanStorage', () => ({
  readActiveLoan: () => null,
  toStoredActiveLoan: (loan: {
    loanId: number;
    amount: number;
    annualRate: number;
    monthlyPayment: number;
    contractDate?: string;
    status: 'ACTIVE' | 'OVERDUE' | 'CLOSED';
  }) => ({
    loanId: loan.loanId,
    remainingPrincipal: loan.amount,
    annualRate: loan.annualRate,
    monthlyPayment: loan.monthlyPayment,
    contractDate: loan.contractDate,
    status: loan.status,
  }),
  writeActiveLoan: (...args: unknown[]) => mockWriteActiveLoan(...args),
}));

vi.mock('@shared/components/AuthImage/AuthImage', () => ({
  AuthImage: ({ fallback }: { fallback: ReactNode }) => <div>{fallback}</div>,
}));

vi.mock('./LoanDetailPanel', () => ({
  LoanDetailPanel: () => <div>상세 패널</div>,
}));

describe('LoanProductsPanel', () => {
  it('shows the active loan summary from a confirmed loan', () => {
    render(
      <LoanProductsPanel
        sessionId={7}
        confirmedLoan={{
          loanId: 15,
          amount: 1_000_000,
          annualRate: 3.1,
          monthlyPayment: 42_000,
          status: 'ACTIVE',
        }}
      />,
    );

    expect(screen.getByTestId('active-loan-card')).toBeInTheDocument();
    expect(screen.getByText('확정된 대출')).toBeInTheDocument();
    expect(screen.getByText('1,000,000원')).toBeInTheDocument();
    expect(screen.getByText('42,000원')).toBeInTheDocument();
  });

  it('repays part of the active loan and updates the remaining principal', async () => {
    mockRepay.mockResolvedValueOnce({
      loanId: 15,
      repaidAmount: 200_000,
      remainingPrincipal: 800_000,
      updatedMonthlyPayment: 31_000,
    });

    render(
      <LoanProductsPanel
        sessionId={7}
        confirmedLoan={{
          loanId: 15,
          amount: 1_000_000,
          annualRate: 3.1,
          monthlyPayment: 42_000,
          status: 'ACTIVE',
        }}
      />,
    );

    fireEvent.change(screen.getByPlaceholderText('상환할 금액 입력'), {
      target: { value: '200000' },
    });
    fireEvent.click(screen.getByRole('button', { name: '상환하기' }));

    await waitFor(() => {
      expect(mockRepay).toHaveBeenCalledWith(15, 200_000);
    });

    expect(await screen.findByText('800,000원')).toBeInTheDocument();
    expect(screen.getByText('31,000원')).toBeInTheDocument();
  });
});
