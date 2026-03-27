import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { IGameInitRepository } from '../repositories/IGameInitRepository';
import type { TargetProperty } from '../entities/Region';

@injectable()
export class GetTargetPropertiesUseCase {
  private readonly repository: IGameInitRepository;

  constructor(
    @inject(DI_TOKENS.IGameInitRepository)
    repository: IGameInitRepository,
  ) {
    this.repository = repository;
  }

  execute(regionCode: string, districtCode: string): Promise<TargetProperty[]> {
    return this.repository.getTargetProperties(regionCode, districtCode);
  }
}
