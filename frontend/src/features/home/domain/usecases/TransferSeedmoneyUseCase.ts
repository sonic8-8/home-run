import type { ISeedmoneyRepository } from '../repositories/ISeedmoneyRepository';
import type { SeedmoneyTransaction } from '../entities/SeedmoneyTransaction';

export class TransferSeedmoneyUseCase {
  private readonly repository: ISeedmoneyRepository;
  constructor(repository: ISeedmoneyRepository) { this.repository = repository; }

  execute(toAccountNumber: string, amount: number): Promise<SeedmoneyTransaction> {
    return this.repository.transfer(toAccountNumber, amount);
  }
}
