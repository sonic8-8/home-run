import type { CardRecommendation } from '../entities/CardRecommendation';

export interface ICardRepository {
  getRecommendations(): Promise<CardRecommendation[]>;
}
