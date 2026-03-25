import type { IGameInitRepository } from '../repositories/IGameInitRepository';
import type { Region } from '../entities/Region';

export class GetRegionsUseCase {
  private readonly repository: IGameInitRepository;
  constructor(repository: IGameInitRepository) { this.repository = repository; }

  execute(): Promise<Region[]> {
    return this.repository.getRegions();
  }
}
