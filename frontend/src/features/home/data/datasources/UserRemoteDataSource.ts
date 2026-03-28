import { apiClient } from '@core/network/apiClient';
import { unwrapApiData, type ApiEnvelope } from '@core/network/apiResponse';
import type { UserMeResponseModel, AssetLinkRequestModel, AssetLinkResponseModel } from '../models/UserModel';

export class UserRemoteDataSource {
  getMe(): Promise<UserMeResponseModel> {
    return apiClient.get<ApiEnvelope<UserMeResponseModel>>('/users/me').then(unwrapApiData);
  }

  linkAssets(body: AssetLinkRequestModel): Promise<AssetLinkResponseModel> {
    return apiClient.post<ApiEnvelope<AssetLinkResponseModel>>('/users/me/asset-link', body).then(unwrapApiData);
  }
}
