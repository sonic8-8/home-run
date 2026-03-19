import type { IRealEstateRepository } from '../repositories/IRealEstateRepository';
import type { PropertyDocument } from '../entities/PropertyDocument';

export class GetDocumentsUseCase {
  constructor(private readonly repository: IRealEstateRepository) {}

  async execute(sessionId: number, propertyId: string): Promise<PropertyDocument[]> {
    return this.repository.getDocuments(sessionId, propertyId);
  }
}
