export interface CardBenefit {
  categoryId: string;
  categoryName: string;
  categoryDescription: string;
  discountRate: number;
  exampleMerchants: string[];
}

export interface CardProduct {
  id: string;
  name: string;
  issuerName: string;
  description: string;
  baselinePerformanceAmount: number | null;
  maxBenefitLimitAmount: number | null;
  imageUrl: string;
  activeBenefits: CardBenefit[];
}
