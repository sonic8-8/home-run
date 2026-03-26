import { apiClient } from '@core/network/apiClient'
import type {
  StockMarketResponseModel,
  StockHoldingsResponseModel,
  StockOrderRequestModel,
  StockOrderResponseModel,
} from '@features/stock/data/models/StockModel'

export class StockRemoteDataSource {
  async getMarket(sessionId: number): Promise<StockMarketResponseModel> {
    return apiClient.get<StockMarketResponseModel>(
      `/api/games/sessions/${sessionId}/stocks/market`,
    )
  }

  async getHoldings(sessionId: number): Promise<StockHoldingsResponseModel> {
    return apiClient.get<StockHoldingsResponseModel>(
      `/api/games/sessions/${sessionId}/stocks/holdings`,
    )
  }

  async order(
    sessionId: number,
    body: StockOrderRequestModel,
  ): Promise<StockOrderResponseModel> {
    return apiClient.post<StockOrderResponseModel>(
      `/api/games/sessions/${sessionId}/stocks/orders`,
      body,
    )
  }
}
