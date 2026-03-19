import type { IRealEstateRepository } from '../repositories/IRealEstateRepository';
import type { PropertyDocument } from '../entities/PropertyDocument';

export class GetDocumentsUseCase {
  private readonly repository: IRealEstateRepository;
  constructor(repository: IRealEstateRepository) { this.repository = repository; }

  async execute(sessionId: number, propertyId: string): Promise<PropertyDocument[]> {
    return this.repository.getDocuments(sessionId, propertyId);
  }
}
