import type { ISpendingRepository } from '../repositories/ISpendingRepository';
import type { Spending } from '../entities/Spending';

export class GetSpendingUseCase {
  private readonly repo: ISpendingRepository;
  constructor(repo: ISpendingRepository) { this.repo = repo; }
  execute(month?: string): Promise<Spending> {
    return this.repo.get(month);
  }
}
