import type { IRealEstateRepository } from '../repositories/IRealEstateRepository';
import type { PurchaseResponse } from '../entities/PropertyDocument';

export class PurchasePropertyUseCase {
  private readonly repository: IRealEstateRepository;
  constructor(repository: IRealEstateRepository) { this.repository = repository; }

  async execute(sessionId: number, propertyId: string, loanId: string): Promise<PurchaseResponse> {
    return this.repository.purchaseProperty(sessionId, propertyId, loanId);
  }
}
