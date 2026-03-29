import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { IEndingRepository } from '../repositories/IEndingRepository';
import type { EndingReport } from '../entities/EndingReport';

@injectable()
export class GetEndingReportUseCase {
  private readonly repository: IEndingRepository;

  constructor(
    @inject(DI_TOKENS.IEndingRepository)
    repository: IEndingRepository,
  ) {
    this.repository = repository;
  }

  async execute(sessionId: number): Promise<EndingReport> {
    return this.repository.getEndingReport(sessionId);
  }
}
