import type {
  LoanProductPageModel,
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

export class LoanRemoteDataSource {
  async getProducts(
    sessionId: number,
    category: string,
    page: number,
    size: number,
  ): Promise<LoanProductPageModel> {
    return apiClient.get<LoanProductPageModel>(
      `/api/games/sessions/${sessionId}/loans/products?category=${category}&page=${page}&size=${size}`,
    );
  }

  async getProductDetail(sessionId: number, productId: string): Promise<LoanProductDetailModel> {
    return apiClient.get<LoanProductDetailModel>(
      `/api/games/sessions/${sessionId}/loans/products/${productId}`,
    );
  }

  async calculate(sessionId: number, body: LoanCalculateRequestModel): Promise<LoanCalculateResponseModel> {
    return apiClient.post<LoanCalculateResponseModel>(
      `/api/games/sessions/${sessionId}/loans/calculate`,
      body,
    );
  }

  async apply(sessionId: number, productId: string, propertyId: string): Promise<LoanApplyResponseModel> {
    return apiClient.post<LoanApplyResponseModel>(
      `/api/games/sessions/${sessionId}/loans/apply`,
      { productId, propertyId },
    );
  }

  async confirm(sessionId: number, body: LoanConfirmRequestModel): Promise<LoanConfirmResponseModel> {
    return apiClient.post<LoanConfirmResponseModel>(
      `/api/games/sessions/${sessionId}/loans/confirm`,
      body,
    );
  }

  async repay(sessionId: number, body: LoanRepayRequestModel): Promise<LoanRepayResponseModel> {
    return apiClient.post<LoanRepayResponseModel>(
      `/api/games/sessions/${sessionId}/loans/repay`,
      body,
    );
  }
}
