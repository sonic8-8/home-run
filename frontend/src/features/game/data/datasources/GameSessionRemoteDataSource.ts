import { injectable } from 'tsyringe';
import type { GameSessionsResponseModel } from '../models/GameSessionModel';

@injectable()
export class GameSessionRemoteDataSource {
  async getSlots(): Promise<GameSessionsResponseModel> {
    // TODO: apiClient.get<GameSessionsResponseModel>('/games/sessions')
    throw new Error('Not implemented: API client not configured yet');
  }
}
