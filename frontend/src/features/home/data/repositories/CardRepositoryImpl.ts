import type { ICardRepository } from '../../domain/repositories/ICardRepository';
import type { CardRecommendation } from '../../domain/entities/CardRecommendation';
import { CardRemoteDataSource } from '../datasources/CardRemoteDataSource';

export class CardRepositoryImpl implements ICardRepository {
  private readonly dataSource: CardRemoteDataSource;
  constructor(dataSource: CardRemoteDataSource) { this.dataSource = dataSource; }

  async getRecommendations(): Promise<CardRecommendation[]> {
    const m = await this.dataSource.getRecommendations();
    return m.recommendations.map((c) => ({
      cardProductId: String(c.cardProductId),
      cardName: c.cardName,
      cardIssuerName: c.cardIssuerName,
      cardDescription: c.cardDescription,
      baselinePerformanceAmount: c.baselinePerformanceAmount,
      maxBenefitLimitAmount: c.maxBenefitLimitAmount,
      cardImageUrl: c.cardImageUrl,
      activeBenefits: c.activeBenefits.map((b) => ({
        categoryId: b.categoryId,
        categoryName: b.categoryName,
        categoryDescription: b.categoryDescription,
        discountRate: b.discountRate,
        exampleMerchants: b.exampleMerchants,
      })),
    }));
  }
}
