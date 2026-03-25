import type { ICardRepository } from '../repositories/ICardRepository';
import type { CardRecommendation } from '../entities/CardRecommendation';

export class GetCardListUseCase {
  private readonly repo: ICardRepository;
  constructor(repo: ICardRepository) { this.repo = repo; }
  execute(): Promise<CardRecommendation[]> {
    return this.repo.getAll();
  }
}
