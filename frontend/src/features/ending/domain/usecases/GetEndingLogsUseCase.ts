import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { IEndingRepository } from '../repositories/IEndingRepository';
import type { EndingTimelinePoint } from '../entities/EndingTimeline';

@injectable()
export class GetEndingLogsUseCase {
  constructor(
    @inject(DI_TOKENS.IEndingRepository)
    private readonly repository: IEndingRepository,
  ) {}

  async execute(sessionId: number): Promise<EndingTimelinePoint[]> {
    return this.repository.getEndingLogs(sessionId);
  }
}
