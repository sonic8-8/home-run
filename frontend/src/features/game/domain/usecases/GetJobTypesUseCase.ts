import type { IGameInitRepository } from '../repositories/IGameInitRepository';
import type { JobTypeInfo } from '../entities/JobTypeInfo';

export class GetJobTypesUseCase {
  private readonly repository: IGameInitRepository;
  constructor(repository: IGameInitRepository) { this.repository = repository; }

  execute(): Promise<JobTypeInfo[]> {
    return this.repository.getJobTypes();
  }
}
