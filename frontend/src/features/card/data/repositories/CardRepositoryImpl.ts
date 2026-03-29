import { inject, injectable } from 'tsyringe';
import type { ICardRepository } from '../../domain/repositories/ICardRepository';
import type { CardProduct, CardBenefit } from '../../domain/entities/Card';
import type { CardModel, CardBenefitModel } from '../models/CardModel';
import { CardRemoteDataSource } from '../datasources/CardRemoteDataSource';

@injectable()
export class CardRepositoryImpl implements ICardRepository {
  private readonly dataSource: CardRemoteDataSource;

  constructor(
    @inject(CardRemoteDataSource)
    dataSource: CardRemoteDataSource,
  ) {
    this.dataSource = dataSource;
  }

  async getCards(): Promise<CardProduct[]> {
    const model = await this.dataSource.getCards();
    return model.cards.map((card) => this.toEntity(card));
  }

  async getRecommendations(): Promise<CardProduct[]> {
    const model = await this.dataSource.getRecommendations();
    return model.recommendations.map((card) => this.toEntity(card));
  }

  private toEntity(model: CardModel): CardProduct {
    return {
      id: String(model.cardProductId),
      name: model.cardName,
      issuerName: model.cardIssuerName,
      description: model.cardDescription,
      baselinePerformanceAmount: model.baselinePerformanceAmount,
      maxBenefitLimitAmount: model.maxBenefitLimitAmount,
      imageUrl: model.cardImageUrl,
      activeBenefits: model.activeBenefits.map((benefit) => this.toBenefit(benefit)),
    };
  }

  private toBenefit(model: CardBenefitModel): CardBenefit {
    return {
      categoryId: model.categoryId,
      categoryName: model.categoryName,
      categoryDescription: model.categoryDescription,
      discountRate: model.discountRate,
      exampleMerchants: model.exampleMerchants,
    };
  }
}
