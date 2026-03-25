export interface LoanRecommendationItemModel {
  productId: string;
  bankName: string;
  bankLogoUrl: string;
  productName: string;
  productType: string;
  minRate: number;
  maxRate: number;
  estimatedRate: number;
  joinWay: string | null;
  creditProductTypeName: string | null;
  averageRate: number | null;
  rateTypeName: string | null;
  repaymentTypeName: string | null;
  loanLimit: string | null;
  mortgageTypeName: string | null;
}

export interface LoanRecommendationResponseModel {
  cssScore: number;
  cssGrade: number;
  cssGradeLabel: string;
  estimatedMinRate: number;
  creditLoans: LoanRecommendationItemModel[];
  jeonseLoans: LoanRecommendationItemModel[];
  mortgageLoans: LoanRecommendationItemModel[];
}
