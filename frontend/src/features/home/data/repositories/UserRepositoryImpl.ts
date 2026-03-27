import type { IUserRepository } from '../../domain/repositories/IUserRepository';
import type { UserMe } from '../../domain/entities/UserMe';
import type { AssetLinkInput } from '../../domain/entities/AssetLinkInput';
import { UserRemoteDataSource } from '../datasources/UserRemoteDataSource';

export class UserRepositoryImpl implements IUserRepository {
  private readonly dataSource: UserRemoteDataSource;
  constructor(dataSource: UserRemoteDataSource) { this.dataSource = dataSource; }

  async getMe(): Promise<UserMe> {
    const m = await this.dataSource.getMe();
    return {
      userId: m.userId,
      email: m.email,
      name: m.name,
      isAssetLinked: m.isAssetLinked,
      totalAssetAmount: m.totalAssetAmount,
      netAssetAmount: m.netAssetAmount,
    };
  }

  async linkAssets(input: AssetLinkInput): Promise<{ isAssetLinked: boolean }> {
    const m = await this.dataSource.linkAssets(input);
    return { isAssetLinked: m.isAssetLinked };
  }
}
