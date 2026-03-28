export interface CardBenefitModel {
  categoryId: string;
  categoryName: string;
  categoryDescription: string;
  discountRate: number;
  exampleMerchants: string[];
}

export interface CardResponseModel {
  cardProductId: number;
  cardName: string;
  cardIssuerName: string;
  cardDescription: string;
  baselinePerformanceAmount: number;
  maxBenefitLimitAmount: number;
  cardImageUrl: string;
  activeBenefits: CardBenefitModel[];
}

export interface CardListResponseModel {
  cards: CardResponseModel[];
}

export interface CardRecommendationsResponseModel {
  recommendations: CardResponseModel[];
}
