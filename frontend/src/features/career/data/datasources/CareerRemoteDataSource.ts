import { injectable } from 'tsyringe';
import { apiClient } from '@core/network/apiClient';
import { unwrapApiData, type ApiEnvelope } from '@core/network/apiResponse';
import type {
  CareerSessionsResponseModel,
  JobOfferListResponseModel,
  JobTransferRequestModel,
  JobTransferResponseModel,
  SalaryNegotiationResponseModel,
} from '../models/CareerModel';

@injectable()
export class CareerRemoteDataSource {
  async getSessions(): Promise<CareerSessionsResponseModel> {
    return apiClient
      .get<ApiEnvelope<CareerSessionsResponseModel>>('/games/sessions')
      .then(unwrapApiData);
  }

  async negotiateSalary(sessionId: number): Promise<SalaryNegotiationResponseModel> {
    return apiClient
      .post<ApiEnvelope<SalaryNegotiationResponseModel>>(
        `/games/sessions/${sessionId}/career/negotiate`,
      )
      .then(unwrapApiData);
  }

  async getJobOffers(sessionId: number): Promise<JobOfferListResponseModel> {
    return apiClient
      .get<ApiEnvelope<JobOfferListResponseModel>>(
        `/games/sessions/${sessionId}/career/job-offers`,
      )
      .then(unwrapApiData);
  }

  async transferJob(
    sessionId: number,
    request: JobTransferRequestModel,
  ): Promise<JobTransferResponseModel> {
    return apiClient
      .post<ApiEnvelope<JobTransferResponseModel>>(
        `/games/sessions/${sessionId}/career/transfer`,
        request,
      )
      .then(unwrapApiData);
  }
}
