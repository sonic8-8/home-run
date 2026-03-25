export interface CreditScore {
  score: number;           // 0~1000
  grade: number;           // 1~5
  gradeLabel: string;      // Exceptional | Very Good | Good | Fair | Poor
  paymentHistory: number;  // /350
  amountsOwed: number;     // /300
  creditLength: number;    // /150
  creditMix: number;       // /100
  newCredit: number;       // /100
  ratingName: string;
  totalAsset: number;
  totalDebt: number;
  netAsset: number;
}
