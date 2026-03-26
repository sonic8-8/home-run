import type { ISeedmoneyRepository } from '../repositories/ISeedmoneyRepository';
import type { SeedMoneyAccount } from '../entities/SeedMoneyAccount';

export class CreateSeedmoneyAccountUseCase {
  private readonly repository: ISeedmoneyRepository;
  constructor(repository: ISeedmoneyRepository) { this.repository = repository; }

  execute(accountTypeUniqueNo: string): Promise<SeedMoneyAccount> {
    return this.repository.create(accountTypeUniqueNo);
  }
}
