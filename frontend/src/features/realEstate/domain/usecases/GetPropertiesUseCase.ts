import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type {
  IRealEstateRepository,
  PropertyListQuery,
} from '../repositories/IRealEstateRepository';
import type { PropertySummary } from '../entities/Property';

@injectable()
export class GetPropertiesUseCase {
  private readonly repository: IRealEstateRepository;

  constructor(
    @inject(DI_TOKENS.IRealEstateRepository)
    repository: IRealEstateRepository,
  ) {
    this.repository = repository;
  }

  async execute(query: PropertyListQuery): Promise<PropertySummary[]> {
    return this.repository.getProperties(query);
  }
}
