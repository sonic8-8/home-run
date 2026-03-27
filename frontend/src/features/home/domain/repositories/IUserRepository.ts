import type { UserMe } from '../entities/UserMe';
import type { AssetLinkInput } from '../entities/AssetLinkInput';

export interface IUserRepository {
  getMe(): Promise<UserMe>;
  linkAssets(input: AssetLinkInput): Promise<{ isAssetLinked: boolean }>;
}
