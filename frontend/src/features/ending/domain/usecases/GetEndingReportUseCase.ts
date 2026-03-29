import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { IEndingRepository } from '../repositories/IEndingRepository';
import type { EndingReport } from '../entities/EndingReport';

@injectable()
export class GetEndingReportUseCase {
  constructor(
    @inject(DI_TOKENS.IEndingRepository)
    private readonly repository: IEndingRepository,
  ) {}

  async execute(sessionId: number): Promise<EndingReport> {
    return this.repository.getEndingReport(sessionId);
  }
}
