import { injectable } from 'tsyringe';
import { apiClient } from '@core/network/apiClient';
import { unwrapApiData, type ApiEnvelope } from '@core/network/apiResponse';
import type {
  EndingLogsResponseModel,
  EndingReportResponseModel,
} from '../models/EndingModel';

@injectable()
export class EndingRemoteDataSource {
  async getEndingReport(sessionId: number): Promise<EndingReportResponseModel> {
    return apiClient.get<ApiEnvelope<EndingReportResponseModel>>(
      `/games/sessions/${sessionId}/ending`,
    ).then(unwrapApiData);
  }

  async getEndingLogs(sessionId: number): Promise<EndingLogsResponseModel> {
    return apiClient.get<ApiEnvelope<EndingLogsResponseModel>>(
      `/games/sessions/${sessionId}/logs`,
    ).then(unwrapApiData);
  }
}
