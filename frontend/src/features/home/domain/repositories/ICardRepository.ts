import type { CardRecommendation } from '../entities/CardRecommendation';

export interface ICardRepository {
  getAll(): Promise<CardRecommendation[]>;
  getRecommendations(): Promise<CardRecommendation[]>;
}
