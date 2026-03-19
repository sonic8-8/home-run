import type { IRealEstateRepository } from '../repositories/IRealEstateRepository';
import type { Property } from '../entities/Property';

export class GetPropertyDetailUseCase {
  constructor(private readonly repository: IRealEstateRepository) {}

  async execute(sessionId: number, propertyId: string): Promise<Property> {
    return this.repository.getPropertyDetail(sessionId, propertyId);
  }
}
