export interface CreditScoreResponseModel {
  score: number;
  grade: number;
  gradeLabel: string;
  paymentHistory: number;
  amountsOwed: number;
  creditLength: number;
  creditMix: number;
  newCredit: number;
  ratingName: string;
  totalAsset: number;
  totalDebt: number;
  netAsset: number;
}
