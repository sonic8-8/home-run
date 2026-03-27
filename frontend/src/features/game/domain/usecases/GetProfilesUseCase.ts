import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { IGameInitRepository } from '../repositories/IGameInitRepository';
import type { ProfileOption } from '../entities/ProfileOption';

@injectable()
export class GetProfilesUseCase {
  private readonly repo: IGameInitRepository;

  constructor(
    @inject(DI_TOKENS.IGameInitRepository)
    repo: IGameInitRepository,
  ) {
    this.repo = repo;
  }

  execute(): Promise<ProfileOption[]> {
    return this.repo.getProfiles();
  }
}
