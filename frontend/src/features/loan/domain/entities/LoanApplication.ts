export type LoanApplicationStatus = 'APPROVED' | 'REJECTED';

export interface LoanApplicationRequestInfo {
  readonly propertyName: string;
  readonly propertyPrice: number;
  readonly applicationDate: string;
}

export interface LoanApplicationResult {
  readonly maxLoanAmount?: number;
  readonly rejectionReason?: string;
}

export interface LoanApplication {
  readonly applicationId: string;
  readonly status: LoanApplicationStatus;
  readonly requestInfo: LoanApplicationRequestInfo;
  readonly result: LoanApplicationResult;
}
