import type { SeedMoneyAccount } from '../entities/SeedMoneyAccount';
import type { SeedmoneyTransaction } from '../entities/SeedmoneyTransaction';

export interface ISeedmoneyRepository {
  getAccount(): Promise<SeedMoneyAccount>;
  transfer(toAccountNumber: string, amount: number): Promise<SeedmoneyTransaction>;
  deposit(fromAccountNumber: string, amount: number): Promise<SeedmoneyTransaction>;
}
