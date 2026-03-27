import { injectable } from 'tsyringe';
import type {
  PropertiesResponseModel,
  PropertyModel,
  PurchaseResponseModel,
  DocumentsResponseModel,
  ContractRequestModel,
  ContractResponseModel,
} from '../models/PropertyModel';
import { apiClient } from '@core/network/apiClient';

@injectable()
export class RealEstateRemoteDataSource {
  async getProperties(sessionId: number, bounds?: string): Promise<PropertiesResponseModel> {
    const query = bounds ? `?bounds=${encodeURIComponent(bounds)}` : '';
    return apiClient.get<PropertiesResponseModel>(
      `/api/games/sessions/${sessionId}/real-estate/properties${query}`,
    );
  }

  async getPropertyDetail(sessionId: number, propertyId: string): Promise<PropertyModel> {
    return apiClient.get<PropertyModel>(
      `/api/games/sessions/${sessionId}/real-estate/properties/${propertyId}`,
    );
  }

  async purchaseProperty(sessionId: number, propertyId: string, loanId: string): Promise<PurchaseResponseModel> {
    return apiClient.post<PurchaseResponseModel>(
      `/api/games/sessions/${sessionId}/real-estate/properties/${propertyId}/purchase`,
      { loanId },
    );
  }

  async getDocuments(sessionId: number, propertyId: string): Promise<DocumentsResponseModel> {
    return apiClient.get<DocumentsResponseModel>(
      `/api/games/sessions/${sessionId}/real-estate/properties/${propertyId}/documents`,
    );
  }

  async contract(sessionId: number, propertyId: string, body: ContractRequestModel): Promise<ContractResponseModel> {
    return apiClient.post<ContractResponseModel>(
      `/api/games/sessions/${sessionId}/real-estate/properties/${propertyId}/contract`,
      body,
    );
  }
}
