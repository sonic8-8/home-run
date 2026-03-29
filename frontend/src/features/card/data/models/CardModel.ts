export interface CardBenefitModel {
  categoryId: string;
  categoryName: string;
  categoryDescription: string;
  discountRate: number;
  exampleMerchants: string[];
}

export interface CardModel {
  cardProductId: number;
  cardName: string;
  cardIssuerName: string;
  cardDescription: string;
  baselinePerformanceAmount: number | null;
  maxBenefitLimitAmount: number | null;
  cardImageUrl: string;
  activeBenefits: CardBenefitModel[];
}

export interface CardListResponseModel {
  cards: CardModel[];
}

export interface CardRecommendationsResponseModel {
  recommendations: CardModel[];
}
