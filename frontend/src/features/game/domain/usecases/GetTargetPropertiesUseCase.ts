import type { IGameInitRepository } from '../repositories/IGameInitRepository';
import type { TargetProperty } from '../entities/Region';

export class GetTargetPropertiesUseCase {
  private readonly repository: IGameInitRepository;
  constructor(repository: IGameInitRepository) { this.repository = repository; }

  execute(regionCode: string, districtCode: string): Promise<TargetProperty[]> {
    return this.repository.getTargetProperties(regionCode, districtCode);
  }
}
