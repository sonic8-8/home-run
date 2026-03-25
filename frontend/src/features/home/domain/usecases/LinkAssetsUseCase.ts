import type { IUserRepository } from '../repositories/IUserRepository';

export class LinkAssetsUseCase {
  constructor(private readonly repo: IUserRepository) {}
  execute(): Promise<{ isAssetLinked: boolean }> {
    return this.repo.linkAssets();
  }
}
