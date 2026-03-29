import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { JobTransferResult } from '../entities/JobTransfer';
import type { ICareerRepository } from '../repositories/ICareerRepository';

@injectable()
export class TransferCareerUseCase {
  private readonly repository: ICareerRepository;

  constructor(
    @inject(DI_TOKENS.ICareerRepository)
    repository: ICareerRepository,
  ) {
    this.repository = repository;
  }

  async execute(sessionId: number, offerId: string): Promise<JobTransferResult> {
    return this.repository.transferJob(sessionId, offerId);
  }
}
