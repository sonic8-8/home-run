import { injectable } from 'tsyringe';
import type {
  PropertyListRequestModel,
  PropertiesResponseModel,
  PropertyModel,
  PurchaseResponseModel,
  RegistryDocumentResponseModel,
  ContractRequestModel,
  ContractResponseModel,
} from '../models/PropertyModel';
import { apiClient } from '@core/network/apiClient';
import { unwrapApiData, type ApiEnvelope } from '@core/network/apiResponse';

@injectable()
export class RealEstateRemoteDataSource {
  async getProperties(query: PropertyListRequestModel): Promise<PropertiesResponseModel> {
    if ('sessionId' in query) {
      return apiClient.get<ApiEnvelope<PropertiesResponseModel>>(
        `/games/sessions/${query.sessionId}/real-estate/properties`,
        query.bounds === undefined
          ? undefined
          : { params: { bounds: query.bounds } },
      ).then(unwrapApiData);
    }

    return apiClient.get<ApiEnvelope<PropertiesResponseModel>>(
      `/games/regions/${query.regionCode}/districts/${query.districtCode}/properties`,
    ).then(unwrapApiData);
  }

  async getPropertyDetail(sessionId: number, propertyId: string): Promise<PropertyModel> {
    return apiClient.get<ApiEnvelope<PropertyModel>>(
      `/games/sessions/${sessionId}/real-estate/properties/${propertyId}`,
    ).then(unwrapApiData);
  }

  async purchaseProperty(sessionId: number, propertyId: string, loanId: string): Promise<PurchaseResponseModel> {
    return apiClient.post<ApiEnvelope<PurchaseResponseModel>>(
      `/games/sessions/${sessionId}/real-estate/properties/${propertyId}/purchase`,
      { loanId },
    ).then(unwrapApiData);
  }

  async getDocuments(sessionId: number, propertyId: string): Promise<RegistryDocumentResponseModel> {
    return apiClient.get<ApiEnvelope<RegistryDocumentResponseModel>>(
      `/games/sessions/${sessionId}/real-estate/properties/${propertyId}/documents`,
    ).then(unwrapApiData);
  }

  async contract(sessionId: number, propertyId: string, body: ContractRequestModel): Promise<ContractResponseModel> {
    return apiClient.post<ApiEnvelope<ContractResponseModel>>(
      `/games/sessions/${sessionId}/real-estate/properties/${propertyId}/contract`,
      body,
    ).then(unwrapApiData);
  }
}
