import { apiClient } from '@core/network/apiClient';
import type {
  SeedmoneyAccountModel,
  SeedmoneyTransactionModel,
  SeedmoneyTransferRequestModel,
  SeedmoneyDepositRequestModel,
} from '../models/SeedmoneyModel';

export class SeedmoneyRemoteDataSource {
  getAccount(): Promise<SeedmoneyAccountModel> {
    return apiClient.get<SeedmoneyAccountModel>('/api/seedmoney/account');
  }

  transfer(body: SeedmoneyTransferRequestModel): Promise<SeedmoneyTransactionModel> {
    return apiClient.post<SeedmoneyTransactionModel>('/api/seedmoney/transfer', body);
  }

  deposit(body: SeedmoneyDepositRequestModel): Promise<SeedmoneyTransactionModel> {
    return apiClient.post<SeedmoneyTransactionModel>('/api/seedmoney/deposit', body);
  }
}
