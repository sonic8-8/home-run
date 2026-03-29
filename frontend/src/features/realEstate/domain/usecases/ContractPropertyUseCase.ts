import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { IRealEstateRepository } from '../repositories/IRealEstateRepository';
import type { ContractResponse } from '../entities/PropertyDocument';

@injectable()
export class ContractPropertyUseCase {
  private readonly repository: IRealEstateRepository;

  constructor(
    @inject(DI_TOKENS.IRealEstateRepository)
    repository: IRealEstateRepository,
  ) {
    this.repository = repository;
  }

  async execute(sessionId: number, propertyId: string, checkedTraps: string[]): Promise<ContractResponse> {
    return this.repository.contract(sessionId, propertyId, checkedTraps);
  }
}
