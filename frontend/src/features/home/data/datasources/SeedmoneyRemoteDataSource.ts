import { apiClient } from '@core/network/apiClient';
import { unwrapApiData, type ApiEnvelope } from '@core/network/apiResponse';
import type {
  SeedmoneyAccountModel,
  SeedmoneyTransactionModel,
  SeedmoneyTransferRequestModel,
  SeedmoneyDepositRequestModel,
  SeedmoneyCreateRequestModel,
} from '../models/SeedmoneyModel';

export class SeedmoneyRemoteDataSource {
  create(body: SeedmoneyCreateRequestModel): Promise<SeedmoneyAccountModel> {
    return apiClient.post<ApiEnvelope<SeedmoneyAccountModel>>('/seedmoney/create', body).then(unwrapApiData);
  }

  getAccount(): Promise<SeedmoneyAccountModel> {
    return apiClient.get<ApiEnvelope<SeedmoneyAccountModel>>('/seedmoney/account').then(unwrapApiData);
  }

  transfer(body: SeedmoneyTransferRequestModel): Promise<SeedmoneyTransactionModel> {
    return apiClient.post<ApiEnvelope<SeedmoneyTransactionModel>>('/seedmoney/transfer', body).then(unwrapApiData);
  }

  deposit(body: SeedmoneyDepositRequestModel): Promise<SeedmoneyTransactionModel> {
    return apiClient.post<ApiEnvelope<SeedmoneyTransactionModel>>('/seedmoney/deposit', body).then(unwrapApiData);
  }
}
