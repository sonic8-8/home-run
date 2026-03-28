import { injectable } from 'tsyringe'
import { apiClient } from '@core/network/apiClient'
import { unwrapApiData, type ApiEnvelope } from '@core/network/apiResponse'
import type {
  GameTurnResponseModel,
  LatestTurnNewsResponseModel,
} from '@features/game/data/models/GameTurnModel'

@injectable()
export class GameTurnRemoteDataSource {
  async getTurn(sessionId: number): Promise<GameTurnResponseModel> {
    return apiClient.get<ApiEnvelope<GameTurnResponseModel>>(
      `/games/sessions/${sessionId}/turn`,
    ).then(unwrapApiData)
  }

  async getLatestNews(sessionId: number): Promise<LatestTurnNewsResponseModel> {
    return apiClient.get<ApiEnvelope<LatestTurnNewsResponseModel>>(
      `/games/sessions/${sessionId}/news/latest`,
    ).then(unwrapApiData)
  }
}
