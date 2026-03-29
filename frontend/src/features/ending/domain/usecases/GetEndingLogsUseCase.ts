import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { IEndingRepository } from '../repositories/IEndingRepository';
import type { EndingTimelinePoint } from '../entities/EndingTimeline';

@injectable()
export class GetEndingLogsUseCase {
  private readonly repository: IEndingRepository;

  constructor(
    @inject(DI_TOKENS.IEndingRepository)
    repository: IEndingRepository,
  ) {
    this.repository = repository;
  }

  async execute(sessionId: number): Promise<EndingTimelinePoint[]> {
    return this.repository.getEndingLogs(sessionId);
  }
}
