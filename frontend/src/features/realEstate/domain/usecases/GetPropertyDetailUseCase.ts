import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { IRealEstateRepository } from '../repositories/IRealEstateRepository';
import type { Property } from '../entities/Property';

@injectable()
export class GetPropertyDetailUseCase {
  private readonly repository: IRealEstateRepository;

  constructor(
    @inject(DI_TOKENS.IRealEstateRepository)
    repository: IRealEstateRepository,
  ) {
    this.repository = repository;
  }

  async execute(sessionId: number, propertyId: string): Promise<Property> {
    return this.repository.getPropertyDetail(sessionId, propertyId);
  }
}
