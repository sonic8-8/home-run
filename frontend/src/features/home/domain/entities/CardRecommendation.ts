export interface CardBenefit {
  categoryId: string;
  categoryName: string;
  categoryDescription: string;
  discountRate: number;
  exampleMerchants: string[];
}

export interface CardRecommendation {
  cardProductId: string;
  cardName: string;
  cardIssuerName: string;
  cardDescription: string;
  baselinePerformanceAmount: number;
  maxBenefitLimitAmount: number;
  cardImageUrl: string;
  activeBenefits: CardBenefit[];
}
