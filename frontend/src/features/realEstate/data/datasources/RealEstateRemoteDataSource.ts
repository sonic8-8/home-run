import { injectable } from 'tsyringe';
import type { PropertyListQuery } from '../../domain/repositories/IRealEstateRepository';
import type {
  PropertiesResponseModel,
  PropertyModel,
  PurchaseResponseModel,
  RegistryDocumentResponseModel,
  ContractRequestModel,
  ContractResponseModel,
} from '../models/PropertyModel';
import { apiClient } from '@core/network/apiClient';

@injectable()
export class RealEstateRemoteDataSource {
  async getProperties(query: PropertyListQuery): Promise<PropertiesResponseModel> {
    if (query.sessionId !== undefined) {
      const boundsQuery = query.bounds
        ? `?bounds=${encodeURIComponent(query.bounds)}`
        : '';

      return apiClient.get<PropertiesResponseModel>(
        `/api/games/sessions/${query.sessionId}/real-estate/properties${boundsQuery}`,
      );
    }

    if (query.regionCode !== undefined && query.districtCode !== undefined) {
      return apiClient.get<PropertiesResponseModel>(
        `/api/games/regions/${query.regionCode}/districts/${query.districtCode}/properties`,
      );
    }

    throw new Error('매물 조회 조건이 부족합니다.');
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

  async getDocuments(sessionId: number, propertyId: string): Promise<RegistryDocumentResponseModel> {
    return apiClient.get<RegistryDocumentResponseModel>(
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
