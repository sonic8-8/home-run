import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { IGameInitRepository } from '../repositories/IGameInitRepository';
import type { Region } from '../entities/Region';

@injectable()
export class GetRegionsUseCase {
  private readonly repository: IGameInitRepository;

  constructor(
    @inject(DI_TOKENS.IGameInitRepository)
    repository: IGameInitRepository,
  ) {
    this.repository = repository;
  }

  execute(): Promise<Region[]> {
    return this.repository.getRegions();
  }
}
