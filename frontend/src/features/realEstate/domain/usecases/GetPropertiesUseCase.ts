import type { IRealEstateRepository } from '../repositories/IRealEstateRepository';
import type { PropertySummary } from '../entities/Property';

export class GetPropertiesUseCase {
  private readonly repository: IRealEstateRepository;
  constructor(repository: IRealEstateRepository) { this.repository = repository; }

  async execute(sessionId: number, bounds?: string): Promise<PropertySummary[]> {
    return this.repository.getProperties(sessionId, bounds);
  }
}
