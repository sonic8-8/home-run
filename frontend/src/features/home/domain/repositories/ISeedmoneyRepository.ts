import type { SeedMoneyAccount } from '../entities/SeedMoneyAccount';
import type { SeedmoneyTransaction } from '../entities/SeedmoneyTransaction';

export interface ISeedmoneyRepository {
  create(accountTypeUniqueNo: string): Promise<SeedMoneyAccount>;
  getAccount(): Promise<SeedMoneyAccount>;
  transfer(toAccountNumber: string, amount: number): Promise<SeedmoneyTransaction>;
  deposit(fromAccountNumber: string, amount: number): Promise<SeedmoneyTransaction>;
}
