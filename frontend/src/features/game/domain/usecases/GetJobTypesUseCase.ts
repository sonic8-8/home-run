import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { IGameInitRepository } from '../repositories/IGameInitRepository';
import type { JobTypeInfo } from '../entities/JobTypeInfo';

@injectable()
export class GetJobTypesUseCase {
  private readonly repository: IGameInitRepository;

  constructor(
    @inject(DI_TOKENS.IGameInitRepository)
    repository: IGameInitRepository,
  ) {
    this.repository = repository;
  }

  execute(): Promise<JobTypeInfo[]> {
    return this.repository.getJobTypes();
  }
}
