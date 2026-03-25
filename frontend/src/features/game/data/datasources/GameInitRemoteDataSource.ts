import { apiClient } from '@core/network/apiClient';
import type {
  CharacterOptionsResponseModel,
  JobTypeOptionsResponseModel,
  RegionListResponseModel,
  DistrictListResponseModel,
  TargetPropertyListResponseModel,
} from '../models/GameInitModel';

export class GameInitRemoteDataSource {
  getCharacters(): Promise<CharacterOptionsResponseModel> {
    return apiClient.get<CharacterOptionsResponseModel>('/api/games/characters');
  }

  getJobTypes(): Promise<JobTypeOptionsResponseModel> {
    return apiClient.get<JobTypeOptionsResponseModel>('/api/games/job-types');
  }

  getRegions(): Promise<RegionListResponseModel> {
    return apiClient.get<RegionListResponseModel>('/api/games/regions');
  }

  getDistricts(regionCode: string): Promise<DistrictListResponseModel> {
    return apiClient.get<DistrictListResponseModel>(`/api/games/regions/${regionCode}/districts`);
  }

  getTargetProperties(regionCode: string, districtCode: string): Promise<TargetPropertyListResponseModel> {
    return apiClient.get<TargetPropertyListResponseModel>(
      `/api/games/regions/${regionCode}/districts/${districtCode}/properties`,
    );
  }
}
