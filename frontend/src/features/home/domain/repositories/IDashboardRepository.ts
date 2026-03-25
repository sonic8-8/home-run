import type { Dashboard } from '../entities/Dashboard';

export interface IDashboardRepository {
  get(): Promise<Dashboard>;
}
