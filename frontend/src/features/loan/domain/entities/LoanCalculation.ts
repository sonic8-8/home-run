export type RepaymentMethod = 'EQUAL_PRINCIPAL_INTEREST' | 'EQUAL_PRINCIPAL' | 'BULLET';

export interface LoanCalculateParams {
  readonly repaymentMethod: RepaymentMethod;
  readonly termMonths: number;
  readonly principal: number;
  readonly annualRate: number;
}

export interface LoanCalculation {
  readonly monthlyPayment: number;
  readonly totalInterest: number;
  readonly totalPayment: number;
}
