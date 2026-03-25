import type { IGameInitRepository } from '../repositories/IGameInitRepository';
import type { ProfileOption } from '../entities/ProfileOption';

export class GetProfilesUseCase {
  private readonly repo: IGameInitRepository;
  constructor(repo: IGameInitRepository) { this.repo = repo; }
  execute(): Promise<ProfileOption[]> {
    return this.repo.getProfiles();
  }
}
