import type { RepaymentMethod } from './LoanCalculation';

export type LoanStatus = 'ACTIVE' | 'OVERDUE' | 'CLOSED';

export interface ActiveLoan {
  readonly loanId: number;
  readonly amount: number;
  readonly annualRate: number;
  readonly monthlyPayment: number;
  readonly contractDate?: string;
  readonly status: LoanStatus;
}

export interface LoanConfirmResult extends ActiveLoan {
  readonly repaymentMethod?: RepaymentMethod;
}

export interface LoanRepayResult {
  readonly loanId: number;
  readonly repaidAmount: number;
  readonly remainingPrincipal: number;
  readonly updatedMonthlyPayment: number;
}
