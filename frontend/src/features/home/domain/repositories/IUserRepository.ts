import type { UserMe } from '../entities/UserMe';

export interface IUserRepository {
  getMe(): Promise<UserMe>;
  linkAssets(): Promise<{ isAssetLinked: boolean }>;
}
