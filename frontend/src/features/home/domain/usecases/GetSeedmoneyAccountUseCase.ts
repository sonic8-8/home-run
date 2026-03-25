import type { ISeedmoneyRepository } from '../repositories/ISeedmoneyRepository';
import type { SeedMoneyAccount } from '../entities/SeedMoneyAccount';

export class GetSeedmoneyAccountUseCase {
  private readonly repository: ISeedmoneyRepository;
  constructor(repository: ISeedmoneyRepository) { this.repository = repository; }

  execute(): Promise<SeedMoneyAccount> {
    return this.repository.getAccount();
  }
}
