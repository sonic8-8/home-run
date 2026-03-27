import type { IUserRepository } from '../repositories/IUserRepository';
import type { AssetLinkInput } from '../entities/AssetLinkInput';

export class LinkAssetsUseCase {
  private readonly repo: IUserRepository;
  constructor(repo: IUserRepository) { this.repo = repo; }
  execute(input: AssetLinkInput): Promise<{ isAssetLinked: boolean }> {
    return this.repo.linkAssets(input);
  }
}
