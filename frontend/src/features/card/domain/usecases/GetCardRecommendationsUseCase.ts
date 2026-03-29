import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { ICardRepository } from '../repositories/ICardRepository';
import type { CardProduct } from '../entities/Card';

@injectable()
export class GetCardRecommendationsUseCase {
  private readonly repository: ICardRepository;

  constructor(
    @inject(DI_TOKENS.ICardRepository)
    repository: ICardRepository,
  ) {
    this.repository = repository;
  }

  async execute(): Promise<CardProduct[]> {
    return this.repository.getRecommendations();
  }
}
