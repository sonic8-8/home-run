import type { IRealEstateRepository } from '../repositories/IRealEstateRepository';
import type { PropertySummary } from '../entities/Property';

export class GetPropertiesUseCase {
  constructor(private readonly repository: IRealEstateRepository) {}

  async execute(sessionId: number, bounds: string): Promise<PropertySummary[]> {
    return this.repository.getProperties(sessionId, bounds);
  }
}
