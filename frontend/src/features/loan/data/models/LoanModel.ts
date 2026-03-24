// ---- Products ----
export interface LoanProductModel {
  productId: string;
  bankName: string;
  bankLogoUrl: string;
  productName: string;
  productType: string;
  minRate: number;
  maxRate: number;
}

export interface LoanProductDetailModel extends LoanProductModel {
  features: string[];
}

export interface LoanProductPageModel {
  content: LoanProductModel[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// ---- Calculate ----
export interface LoanCalculateRequestModel {
  repaymentMethod: string;
  termMonths: number;
  principal: number;
  annualRate: number;
}

export interface LoanCalculateResponseModel {
  monthlyPayment: number;
  totalInterest: number;
  totalPayment: number;
}

// ---- Apply ----
export interface LoanApplyRequestModel {
  productId: string;
  propertyId: string;
}

export interface LoanApplyRequestInfoModel {
  propertyName: string;
  propertyPrice: number;
  applicationDate: string;
}

export interface LoanApplyResultModel {
  maxLoanAmount?: number;
  rejectionReason?: string;
}

export interface LoanApplyResponseModel {
  applicationId: string;
  status: string;
  requestInfo: LoanApplyRequestInfoModel;
  result: LoanApplyResultModel;
}

// ---- Confirm ----
export interface LoanConfirmRequestModel {
  applicationId: string;
  requestedAmount: number;
  agreed: boolean;
}

export interface LoanConfirmResponseModel {
  loanId: number;
  amount: number;
  annualRate: number;
  monthlyPayment: number;
  contractDate?: string;
  status: string;
}

// ---- Repay ----
export interface LoanRepayRequestModel {
  loanId: number;
  amount: number;
}

export interface LoanRepayResponseModel {
  loanId: number;
  repaidAmount: number;
  remainingPrincipal: number;
  updatedMonthlyPayment: number;
}
