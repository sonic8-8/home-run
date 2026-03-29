import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { ICareerRepository } from '../repositories/ICareerRepository';
import type { SalaryNegotiationResult } from '../entities/SalaryNegotiation';

@injectable()
export class NegotiateSalaryUseCase {
  private readonly repository: ICareerRepository;

  constructor(
    @inject(DI_TOKENS.ICareerRepository)
    repository: ICareerRepository,
  ) {
    this.repository = repository;
  }

  async execute(sessionId: number): Promise<SalaryNegotiationResult> {
    return this.repository.negotiateSalary(sessionId);
  }
}
