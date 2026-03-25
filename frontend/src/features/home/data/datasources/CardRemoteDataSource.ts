import { apiClient } from '@core/network/apiClient';
import type { CardRecommendationsResponseModel } from '../models/CardModel';

export class CardRemoteDataSource {
  getRecommendations(): Promise<CardRecommendationsResponseModel> {
    return apiClient.get<CardRecommendationsResponseModel>('/api/home/card-recommendations');
  }
}
