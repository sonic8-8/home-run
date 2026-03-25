import type { Spending } from '../entities/Spending';

export interface ISpendingRepository {
  get(month?: string): Promise<Spending>;
}
