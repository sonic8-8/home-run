import { injectable } from 'tsyringe';
import type {
  CreateGameSessionRequestModel,
  CreateGameSessionResponseModel,
  GameSessionsResponseModel,
} from '../models/GameSessionModel';
import { apiClient } from '@core/network/apiClient';

@injectable()
export class GameSessionRemoteDataSource {
  async createSession(
    request: CreateGameSessionRequestModel,
  ): Promise<CreateGameSessionResponseModel> {
    return apiClient.post<CreateGameSessionResponseModel>(
      '/api/games/sessions',
      request,
    );
  }

  async getSlots(): Promise<GameSessionsResponseModel> {
    return apiClient.get<GameSessionsResponseModel>('/api/games/sessions');
  }
}
