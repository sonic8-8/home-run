import type { IGameInitRepository } from '../repositories/IGameInitRepository';
import type { District } from '../entities/Region';

export class GetDistrictsUseCase {
  private readonly repository: IGameInitRepository;
  constructor(repository: IGameInitRepository) { this.repository = repository; }

  execute(regionCode: string): Promise<District[]> {
    return this.repository.getDistricts(regionCode);
  }
}
