import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { IRealEstateRepository } from '../repositories/IRealEstateRepository';
import type { RegistryDocument } from '../entities/PropertyDocument';

@injectable()
export class GetDocumentsUseCase {
  private readonly repository: IRealEstateRepository;
  constructor(
    @inject(DI_TOKENS.IRealEstateRepository)
    repository: IRealEstateRepository,
  ) { this.repository = repository; }

  async execute(sessionId: number, propertyId: string): Promise<RegistryDocument> {
    return this.repository.getDocuments(sessionId, propertyId);
  }
}
