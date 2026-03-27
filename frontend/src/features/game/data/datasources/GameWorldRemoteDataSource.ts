import { injectable } from 'tsyringe'
import { apiClient } from '@core/network/apiClient'
import type {
  GameTurnResponseModel,
  LatestTurnNewsResponseModel,
} from '@features/game/data/models/GameWorldModel'

@injectable()
export class GameWorldRemoteDataSource {
  async getTurn(sessionId: number): Promise<GameTurnResponseModel> {
    return apiClient.get<GameTurnResponseModel>(
      `/api/games/sessions/${sessionId}/turn`,
    )
  }

  async getLatestNews(sessionId: number): Promise<LatestTurnNewsResponseModel> {
    return apiClient.get<LatestTurnNewsResponseModel>(
      `/api/games/sessions/${sessionId}/news/latest`,
    )
  }
}
