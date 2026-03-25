import type { IDashboardRepository } from '../repositories/IDashboardRepository';
import type { Dashboard } from '../entities/Dashboard';

export class GetDashboardUseCase {
  private readonly repo: IDashboardRepository;
  constructor(repo: IDashboardRepository) { this.repo = repo; }
  execute(): Promise<Dashboard> {
    return this.repo.get();
  }
}
