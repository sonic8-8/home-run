export interface CardResponseModel {
  cardId: string;
  cardName: string;
  cardImageUrl: string;
  annualFee: number;
  summary: string;
}

export interface CardRecommendationsResponseModel {
  recommendations: CardResponseModel[];
}
