import type { ICardRepository } from '../repositories/ICardRepository';
import type { MyCard } from '../entities/MyCard';

export class GetOwnedCardsUseCase {
  private readonly repository: ICardRepository;

  constructor(repository: ICardRepository) {
    this.repository = repository;
  }

  execute(): Promise<MyCard[]> {
    return this.repository.getOwned();
  }
}
