import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { ICardRepository } from '../repositories/ICardRepository';
import type { OwnedCard } from '../entities/Card';

@injectable()
export class ApplyCardUseCase {
  private readonly repository: ICardRepository;

  constructor(
    @inject(DI_TOKENS.ICardRepository)
    repository: ICardRepository,
  ) {
    this.repository = repository;
  }

  async execute(cardProductId: string): Promise<OwnedCard> {
    return this.repository.applyCard(cardProductId);
  }
}
