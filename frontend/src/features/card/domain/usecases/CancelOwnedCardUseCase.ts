import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { ICardRepository } from '../repositories/ICardRepository';

@injectable()
export class CancelOwnedCardUseCase {
  private readonly repository: ICardRepository;

  constructor(
    @inject(DI_TOKENS.ICardRepository)
    repository: ICardRepository,
  ) {
    this.repository = repository;
  }

  async execute(ownedCardId: string): Promise<void> {
    await this.repository.cancelOwnedCard(ownedCardId);
  }
}
