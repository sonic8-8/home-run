import type { ICardRepository } from '../../domain/repositories/ICardRepository';
import type { CardRecommendation } from '../../domain/entities/CardRecommendation';
import { CardRemoteDataSource } from '../datasources/CardRemoteDataSource';

export class CardRepositoryImpl implements ICardRepository {
  private readonly dataSource: CardRemoteDataSource;
  constructor(dataSource: CardRemoteDataSource) { this.dataSource = dataSource; }

  private mapCard(c: { cardProductId: number; cardName: string; cardIssuerName: string; cardDescription: string; baselinePerformanceAmount: number; maxBenefitLimitAmount: number; cardImageUrl: string; activeBenefits: { categoryId: string; categoryName: string; categoryDescription: string; discountRate: number; exampleMerchants: string[] }[] }): CardRecommendation {
    return {
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
    };
  }

  async getAll(): Promise<CardRecommendation[]> {
    const m = await this.dataSource.getAll();
    return m.cards.map((c) => this.mapCard(c));
  }

  async getRecommendations(): Promise<CardRecommendation[]> {
    const m = await this.dataSource.getRecommendations();
    return m.recommendations.map((c) => this.mapCard(c));
  }
}
