import type { ICardRepository } from '../repositories/ICardRepository';
import type { MyCard } from '../entities/MyCard';

export class ApplyCardUseCase {
  private readonly repository: ICardRepository;

  constructor(repository: ICardRepository) {
    this.repository = repository;
  }

  execute(cardProductId: string): Promise<MyCard> {
    return this.repository.apply(cardProductId);
  }
}
