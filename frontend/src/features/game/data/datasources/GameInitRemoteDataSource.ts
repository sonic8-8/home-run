import { injectable } from 'tsyringe';
import { apiClient } from '@core/network/apiClient';
import { unwrapApiData, type ApiEnvelope } from '@core/network/apiResponse';
import type {
  CharacterOptionsResponseModel,
  JobTypeOptionsResponseModel,
  ProfileOptionsResponseModel,
  RegionListResponseModel,
  DistrictListResponseModel,
  TargetPropertyListResponseModel,
} from '../models/GameInitModel';

@injectable()
export class GameInitRemoteDataSource {
  getCharacters(): Promise<CharacterOptionsResponseModel> {
    return apiClient.get<ApiEnvelope<CharacterOptionsResponseModel>>('/games/characters').then(unwrapApiData);
  }

  getJobTypes(): Promise<JobTypeOptionsResponseModel> {
    return apiClient.get<ApiEnvelope<JobTypeOptionsResponseModel>>('/games/job-types').then(unwrapApiData);
  }

  getRegions(): Promise<RegionListResponseModel> {
    return apiClient.get<ApiEnvelope<RegionListResponseModel>>('/games/regions').then(unwrapApiData);
  }

  getDistricts(regionCode: string): Promise<DistrictListResponseModel> {
    return apiClient.get<ApiEnvelope<DistrictListResponseModel>>(`/games/regions/${regionCode}/districts`).then(unwrapApiData);
  }

  getTargetProperties(regionCode: string, districtCode: string): Promise<TargetPropertyListResponseModel> {
    return apiClient.get<ApiEnvelope<TargetPropertyListResponseModel>>(
      `/games/regions/${regionCode}/districts/${districtCode}/properties`,
    ).then(unwrapApiData);
  }

  getProfiles(): Promise<ProfileOptionsResponseModel> {
    return apiClient.get<ApiEnvelope<ProfileOptionsResponseModel>>('/games/profiles').then(unwrapApiData);
  }
}
