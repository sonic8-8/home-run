import { apiClient } from '@core/network/apiClient';
import type { UserMeResponseModel, AssetLinkResponseModel } from '../models/UserModel';

export class UserRemoteDataSource {
  getMe(): Promise<UserMeResponseModel> {
    return apiClient.get<UserMeResponseModel>('/api/users/me');
  }

  linkAssets(): Promise<AssetLinkResponseModel> {
    return apiClient.post<AssetLinkResponseModel>('/api/users/me/asset-link');
  }
}
