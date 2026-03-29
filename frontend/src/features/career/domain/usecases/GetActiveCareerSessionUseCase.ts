import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { CareerSession } from '../entities/CareerSession';
import type { ICareerRepository } from '../repositories/ICareerRepository';

@injectable()
export class GetActiveCareerSessionUseCase {
  private readonly repository: ICareerRepository;

  constructor(
    @inject(DI_TOKENS.ICareerRepository)
    repository: ICareerRepository,
  ) {
    this.repository = repository;
  }

  async execute(): Promise<CareerSession | null> {
    return this.repository.getActiveSession();
  }
}
