import { injectable } from 'tsyringe';
import type { GameSessionsResponseModel } from '../models/GameSessionModel';
import { apiClient } from '@core/network/apiClient';

@injectable()
export class GameSessionRemoteDataSource {
  async getSlots(): Promise<GameSessionsResponseModel> {
    return apiClient.get<GameSessionsResponseModel>('/api/games/sessions');
  }
}
