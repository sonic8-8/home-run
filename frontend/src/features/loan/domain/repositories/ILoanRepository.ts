import type { LoanCategory, LoanProductDetail, LoanProductPage } from '../entities/LoanProduct';
import type { LoanCalculateParams, LoanCalculation } from '../entities/LoanCalculation';
import type { LoanApplication } from '../entities/LoanApplication';
import type { LoanConfirmResult, LoanRepayResult } from '../entities/ActiveLoan';

export interface ILoanRepository {
  getProducts(sessionId: number, category: LoanCategory, page: number, size: number): Promise<LoanProductPage>;
  getProductDetail(sessionId: number, productId: string): Promise<LoanProductDetail>;
  calculate(sessionId: number, params: LoanCalculateParams): Promise<LoanCalculation>;
  apply(sessionId: number, productId: string, propertyId: string): Promise<LoanApplication>;
  confirm(sessionId: number, applicationId: number, requestedAmount: number, agreed: boolean): Promise<LoanConfirmResult>;
  repay(sessionId: number, loanId: number, amount: number): Promise<LoanRepayResult>;
}
