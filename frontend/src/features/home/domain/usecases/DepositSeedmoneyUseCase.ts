import type { ISeedmoneyRepository } from '../repositories/ISeedmoneyRepository';
import type { SeedmoneyTransaction } from '../entities/SeedmoneyTransaction';

export class DepositSeedmoneyUseCase {
  private readonly repository: ISeedmoneyRepository;
  constructor(repository: ISeedmoneyRepository) { this.repository = repository; }

  execute(fromAccountNumber: string, amount: number): Promise<SeedmoneyTransaction> {
    return this.repository.deposit(fromAccountNumber, amount);
  }
}
