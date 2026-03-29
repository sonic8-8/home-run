import { injectable } from 'tsyringe';
import type {
  LoanProductListModel,
  LoanProductDetailModel,
  LoanCalculateRequestModel,
  LoanCalculateResponseModel,
  LoanApplyResponseModel,
  LoanConfirmRequestModel,
  LoanConfirmResponseModel,
  LoanRepayRequestModel,
  LoanRepayResponseModel,
} from '../models/LoanModel';
import { apiClient } from '@core/network/apiClient';
import { unwrapApiData, type ApiEnvelope } from '@core/network/apiResponse';

@injectable()
export class LoanRemoteDataSource {
  async getProducts(
    sessionId: number,
    category: string,
    page: number,
    size: number,
  ): Promise<LoanProductListModel> {
    return apiClient.get<ApiEnvelope<LoanProductListModel>>(
      `/games/sessions/${sessionId}/loans/products`,
      { params: { category, page, size } },
    ).then(unwrapApiData);
  }

  async getProductDetail(sessionId: number, productId: string): Promise<LoanProductDetailModel> {
    return apiClient.get<ApiEnvelope<LoanProductDetailModel>>(
      `/games/sessions/${sessionId}/loans/products/${productId}`,
    ).then(unwrapApiData);
  }

  async calculate(sessionId: number, body: LoanCalculateRequestModel): Promise<LoanCalculateResponseModel> {
    return apiClient.post<ApiEnvelope<LoanCalculateResponseModel>>(
      `/games/sessions/${sessionId}/loans/calculate`,
      body,
    ).then(unwrapApiData);
  }

  async apply(sessionId: number, productId: string, propertyId: string): Promise<LoanApplyResponseModel> {
    return apiClient.post<ApiEnvelope<LoanApplyResponseModel>>(
      `/games/sessions/${sessionId}/loans/apply`,
      { productId, propertyId },
    ).then(unwrapApiData);
  }

  async confirm(sessionId: number, body: LoanConfirmRequestModel): Promise<LoanConfirmResponseModel> {
    return apiClient.post<ApiEnvelope<LoanConfirmResponseModel>>(
      `/games/sessions/${sessionId}/loans/confirm`,
      body,
    ).then(unwrapApiData);
  }

  async repay(sessionId: number, body: LoanRepayRequestModel): Promise<LoanRepayResponseModel> {
    return apiClient.post<ApiEnvelope<LoanRepayResponseModel>>(
      `/games/sessions/${sessionId}/loans/repay`,
      body,
    ).then(unwrapApiData);
  }
}
