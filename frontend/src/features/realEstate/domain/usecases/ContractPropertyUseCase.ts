import type { IRealEstateRepository } from '../repositories/IRealEstateRepository';
import type { ContractResponse } from '../entities/PropertyDocument';

export class ContractPropertyUseCase {
  private readonly repository: IRealEstateRepository;
  constructor(repository: IRealEstateRepository) { this.repository = repository; }

  async execute(sessionId: number, propertyId: string, checkedTraps: string[]): Promise<ContractResponse> {
    return this.repository.contract(sessionId, propertyId, checkedTraps);
  }
}
