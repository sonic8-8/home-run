import { apiClient } from '@core/network/apiClient';
import { unwrapApiData, type ApiEnvelope } from '@core/network/apiResponse';
import type { CardListResponseModel, CardRecommendationsResponseModel } from '../models/CardModel';

export class CardRemoteDataSource {
  getAll(): Promise<CardListResponseModel> {
    return apiClient.get<ApiEnvelope<CardListResponseModel>>('/cards').then(unwrapApiData);
  }

  getRecommendations(): Promise<CardRecommendationsResponseModel> {
    return apiClient.get<ApiEnvelope<CardRecommendationsResponseModel>>('/cards/recommendations').then(unwrapApiData);
  }
}
