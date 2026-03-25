import type { ISeedmoneyRepository } from '../../domain/repositories/ISeedmoneyRepository';
import type { SeedMoneyAccount } from '../../domain/entities/SeedMoneyAccount';
import type { SeedmoneyTransaction } from '../../domain/entities/SeedmoneyTransaction';
import { SeedmoneyRemoteDataSource } from '../datasources/SeedmoneyRemoteDataSource';

export class SeedmoneyRepositoryImpl implements ISeedmoneyRepository {
  private readonly dataSource: SeedmoneyRemoteDataSource;
  constructor(dataSource: SeedmoneyRemoteDataSource) { this.dataSource = dataSource; }

  async getAccount(): Promise<SeedMoneyAccount> {
    const m = await this.dataSource.getAccount();
    return { bankName: m.bankName, accountNumber: m.accountNumber, balance: m.balance };
  }

  async transfer(toAccountNumber: string, amount: number): Promise<SeedmoneyTransaction> {
    const m = await this.dataSource.transfer({ toAccountNumber, amount });
    return { transactionId: m.transactionId, remainingBalance: m.remainingBalance };
  }

  async deposit(fromAccountNumber: string, amount: number): Promise<SeedmoneyTransaction> {
    const m = await this.dataSource.deposit({ fromAccountNumber, amount });
    return { transactionId: m.transactionId, remainingBalance: m.remainingBalance };
  }
}
