import { injectable } from 'tsyringe'
import { apiClient } from '@core/network/apiClient'
import { unwrapApiData, type ApiEnvelope } from '@core/network/apiResponse'
import type {
  GameTurnResponseModel,
  LatestTurnNewsResponseModel,
  TurnActionsResponseModel,
  TurnCommitResponseModel,
  TurnPreviewResponseModel,
} from '@features/game/data/models/GameTurnModel'
import type { SubmitTurnSlotsRequestModel } from '@features/game/data/models/GameTurnRequestModel'

@injectable()
export class GameTurnRemoteDataSource {
  async getTurn(sessionId: number): Promise<GameTurnResponseModel> {
    return apiClient.get<ApiEnvelope<GameTurnResponseModel>>(
      `/games/sessions/${sessionId}/turn`,
    ).then(unwrapApiData)
  }

  async getAvailableActions(sessionId: number): Promise<TurnActionsResponseModel> {
    return apiClient.get<ApiEnvelope<TurnActionsResponseModel>>(
      `/games/sessions/${sessionId}/turn/actions`,
    ).then(unwrapApiData)
  }

  async getLatestNews(sessionId: number): Promise<LatestTurnNewsResponseModel> {
    return apiClient.get<ApiEnvelope<LatestTurnNewsResponseModel>>(
      `/games/sessions/${sessionId}/news/latest`,
    ).then(unwrapApiData)
  }

  async submitTurnSlots(
    sessionId: number,
    request: SubmitTurnSlotsRequestModel,
  ): Promise<TurnPreviewResponseModel> {
    return apiClient.post<ApiEnvelope<TurnPreviewResponseModel>>(
      `/games/sessions/${sessionId}/turn/slots`,
      request,
    ).then(unwrapApiData)
  }

  async commitTurn(sessionId: number): Promise<TurnCommitResponseModel> {
    return apiClient.post<ApiEnvelope<TurnCommitResponseModel>>(
      `/games/sessions/${sessionId}/turn/commit`,
    ).then(unwrapApiData)
  }
}
