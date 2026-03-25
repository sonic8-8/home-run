import type { ICardRepository } from '../../domain/repositories/ICardRepository';
import type { CardRecommendation } from '../../domain/entities/CardRecommendation';
import { CardRemoteDataSource } from '../datasources/CardRemoteDataSource';

export class CardRepositoryImpl implements ICardRepository {
  private readonly dataSource: CardRemoteDataSource;
  constructor(dataSource: CardRemoteDataSource) { this.dataSource = dataSource; }

  async getRecommendations(): Promise<CardRecommendation[]> {
    const m = await this.dataSource.getRecommendations();
    return m.recommendations.map((c) => ({
      cardId: c.cardId,
      cardName: c.cardName,
      cardImageUrl: c.cardImageUrl,
      annualFee: c.annualFee,
      summary: c.summary,
    }));
  }
}
