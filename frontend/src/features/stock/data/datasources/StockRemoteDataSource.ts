import { injectable } from 'tsyringe';
import { apiClient } from '@core/network/apiClient'
import { unwrapApiData, type ApiEnvelope } from '@core/network/apiResponse'
import type {
  StockMarketResponseModel,
  StockHoldingsResponseModel,
  StockOrderRequestModel,
  StockOrderResponseModel,
} from '@features/stock/data/models/StockModel'

@injectable()
export class StockRemoteDataSource {
  async getMarket(sessionId: number): Promise<StockMarketResponseModel> {
    return apiClient.get<ApiEnvelope<StockMarketResponseModel>>(
      `/games/sessions/${sessionId}/stocks/market`,
    ).then(unwrapApiData)
  }

  async getHoldings(sessionId: number): Promise<StockHoldingsResponseModel> {
    return apiClient.get<ApiEnvelope<StockHoldingsResponseModel>>(
      `/games/sessions/${sessionId}/stocks/holdings`,
    ).then(unwrapApiData)
  }

  async order(
    sessionId: number,
    body: StockOrderRequestModel,
  ): Promise<StockOrderResponseModel> {
    return apiClient.post<ApiEnvelope<StockOrderResponseModel>>(
      `/games/sessions/${sessionId}/stocks/orders`,
      body,
    ).then(unwrapApiData)
  }
}
