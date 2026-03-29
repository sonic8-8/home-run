import type { ICardRepository } from '../repositories/ICardRepository';

export class CancelOwnedCardUseCase {
  private readonly repository: ICardRepository;

  constructor(repository: ICardRepository) {
    this.repository = repository;
  }

  execute(ownedCardId: string): Promise<void> {
    return this.repository.cancel(ownedCardId);
  }
}
