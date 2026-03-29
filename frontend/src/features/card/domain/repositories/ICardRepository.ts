import type { CardProduct } from '../entities/Card';

export interface ICardRepository {
  getCards(): Promise<CardProduct[]>;
  getRecommendations(): Promise<CardProduct[]>;
}
