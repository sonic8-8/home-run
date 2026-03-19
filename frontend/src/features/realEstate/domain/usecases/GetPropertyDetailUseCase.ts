import type { IRealEstateRepository } from '../repositories/IRealEstateRepository';
import type { Property } from '../entities/Property';

export class GetPropertyDetailUseCase {
  private readonly repository: IRealEstateRepository;
  constructor(repository: IRealEstateRepository) { this.repository = repository; }

  async execute(sessionId: number, propertyId: string): Promise<Property> {
    return this.repository.getPropertyDetail(sessionId, propertyId);
  }
}
