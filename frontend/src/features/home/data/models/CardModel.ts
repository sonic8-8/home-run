export interface CardRecommendationModel {
  cardId: string;
  cardName: string;
  cardImageUrl: string;
  annualFee: number;
  summary: string;
}

export interface CardRecommendationsResponseModel {
  recommendations: CardRecommendationModel[];
}
