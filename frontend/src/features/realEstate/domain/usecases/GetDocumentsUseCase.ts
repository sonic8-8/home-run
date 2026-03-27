import type { IRealEstateRepository } from '../repositories/IRealEstateRepository';
import type { RegistryDocument } from '../entities/PropertyDocument';

export class GetDocumentsUseCase {
  private readonly repository: IRealEstateRepository;
  constructor(repository: IRealEstateRepository) { this.repository = repository; }

  async execute(sessionId: number, propertyId: string): Promise<RegistryDocument> {
    return this.repository.getDocuments(sessionId, propertyId);
  }
}
