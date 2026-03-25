export type LoanProductType = '개인신용대출' | '전세자금대출' | '주택담보대출';

export interface LoanRecommendation {
  productId: string;
  bankName: string;
  bankLogoUrl: string;
  productName: string;
  productType: LoanProductType;
  minRate: number;
  maxRate: number;
  estimatedRate: number;
  url: string;
}

export interface LoanRecommendationData {
  cssScore: number;
  cssGrade: number;
  cssGradeLabel: string;
  estimatedMinRate: number;
  creditLoans: LoanRecommendation[];
  jeonseLoans: LoanRecommendation[];
  mortgageLoans: LoanRecommendation[];
}
