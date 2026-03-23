export type LoanCategory = 'ALL' | 'CREDIT' | 'JEONSE' | 'MORTGAGE';

export interface LoanProduct {
  readonly productId: string;
  readonly bankName: string;
  readonly bankLogoUrl: string;
  readonly productName: string;
  readonly productType: LoanCategory;
  readonly minRate: number;
  readonly maxRate: number;
}

export interface LoanProductDetail extends LoanProduct {
  readonly features: string[];
}

export interface LoanProductPage {
  readonly content: LoanProduct[];
  readonly page: number;
  readonly size: number;
  readonly totalElements: number;
  readonly totalPages: number;
}
