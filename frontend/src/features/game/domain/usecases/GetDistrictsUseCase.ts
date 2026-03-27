import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { IGameInitRepository } from '../repositories/IGameInitRepository';
import type { District } from '../entities/Region';

@injectable()
export class GetDistrictsUseCase {
  private readonly repository: IGameInitRepository;

  constructor(
    @inject(DI_TOKENS.IGameInitRepository)
    repository: IGameInitRepository,
  ) {
    this.repository = repository;
  }

  execute(regionCode: string): Promise<District[]> {
    return this.repository.getDistricts(regionCode);
  }
}
