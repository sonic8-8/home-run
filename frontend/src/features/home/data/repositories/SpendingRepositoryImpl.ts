import type { ISpendingRepository } from '../../domain/repositories/ISpendingRepository';
import type { Spending } from '../../domain/entities/Spending';
import { SpendingRemoteDataSource } from '../datasources/SpendingRemoteDataSource';

export class SpendingRepositoryImpl implements ISpendingRepository {
  private readonly dataSource: SpendingRemoteDataSource;
  constructor(dataSource: SpendingRemoteDataSource) { this.dataSource = dataSource; }

  async get(month?: string): Promise<Spending> {
    const m = await this.dataSource.get(month);
    return {
      month: m.month,
      totalExpense: m.totalExpense,
      categories: m.categories.map((c) => ({
        category: c.category,
        categoryName: c.categoryName,
        amount: c.amount,
        ratio: c.ratio,
      })),
    };
  }
}
