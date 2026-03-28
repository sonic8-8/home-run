import { injectable } from 'tsyringe';
import type {
  CreateGameSessionRequestModel,
  CreateGameSessionResponseModel,
  GameSessionsResponseModel,
} from '../models/GameSessionModel';
import { apiClient } from '@core/network/apiClient';
import { unwrapApiData, type ApiEnvelope } from '@core/network/apiResponse';

@injectable()
export class GameSessionRemoteDataSource {
  async createSession(
    request: CreateGameSessionRequestModel,
  ): Promise<CreateGameSessionResponseModel> {
    return apiClient.post<ApiEnvelope<CreateGameSessionResponseModel>>(
      '/games/sessions',
      request,
    ).then(unwrapApiData);
  }

  async getSlots(): Promise<GameSessionsResponseModel> {
    return apiClient.get<ApiEnvelope<GameSessionsResponseModel>>('/games/sessions').then(unwrapApiData);
  }
}
