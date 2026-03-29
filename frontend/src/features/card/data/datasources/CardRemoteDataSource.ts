import { injectable } from 'tsyringe';
import { apiClient } from '@core/network/apiClient';
import { unwrapApiData, type ApiEnvelope } from '@core/network/apiResponse';
import type { CardListResponseModel, CardRecommendationsResponseModel } from '../models/CardModel';

@injectable()
export class CardRemoteDataSource {
  async getCards(): Promise<CardListResponseModel> {
    return apiClient
      .get<ApiEnvelope<CardListResponseModel>>('/cards')
      .then(unwrapApiData);
  }

  async getRecommendations(): Promise<CardRecommendationsResponseModel> {
    return apiClient
      .get<ApiEnvelope<CardRecommendationsResponseModel>>('/cards/recommendations')
      .then(unwrapApiData);
  }
}
