import { apiClient } from '@core/network/apiClient';
import type { CardResponseModel, CardRecommendationsResponseModel } from '../models/CardModel';

export interface CardListResponseModel {
  cards: CardResponseModel[];
}

export class CardRemoteDataSource {
  getAll(): Promise<CardListResponseModel> {
    return apiClient.get<CardListResponseModel>('/api/cards');
  }

  getRecommendations(): Promise<CardRecommendationsResponseModel> {
    return apiClient.get<CardRecommendationsResponseModel>('/api/cards/recommendations');
  }
}
