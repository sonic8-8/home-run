import type { IUserRepository } from '../repositories/IUserRepository';

export class LinkAssetsUseCase {
  private readonly repo: IUserRepository;
  constructor(repo: IUserRepository) { this.repo = repo; }
  execute(): Promise<{ isAssetLinked: boolean }> {
    return this.repo.linkAssets();
  }
}
