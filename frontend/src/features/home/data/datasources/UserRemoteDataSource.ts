import { apiClient } from '@core/network/apiClient';
import type { UserMeResponseModel, AssetLinkRequestModel, AssetLinkResponseModel } from '../models/UserModel';

export class UserRemoteDataSource {
  getMe(): Promise<UserMeResponseModel> {
    return apiClient.get<UserMeResponseModel>('/api/users/me');
  }

  linkAssets(body: AssetLinkRequestModel): Promise<AssetLinkResponseModel> {
    return apiClient.post<AssetLinkResponseModel>('/api/users/me/asset-link', body);
  }
}
