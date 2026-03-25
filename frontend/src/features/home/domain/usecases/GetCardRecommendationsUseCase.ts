import type { ICardRepository } from '../repositories/ICardRepository';
import type { CardRecommendation } from '../entities/CardRecommendation';

export class GetCardRecommendationsUseCase {
  private readonly repository: ICardRepository;
  constructor(repository: ICardRepository) { this.repository = repository; }

  execute(): Promise<CardRecommendation[]> {
    return this.repository.getRecommendations();
  }
}
