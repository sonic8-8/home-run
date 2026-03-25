import type { IDashboardRepository } from '../../domain/repositories/IDashboardRepository';
import type { Dashboard } from '../../domain/entities/Dashboard';
import { DashboardRemoteDataSource } from '../datasources/DashboardRemoteDataSource';

export class DashboardRepositoryImpl implements IDashboardRepository {
  private readonly dataSource: DashboardRemoteDataSource;
  constructor(dataSource: DashboardRemoteDataSource) { this.dataSource = dataSource; }

  async get(): Promise<Dashboard> {
    const m = await this.dataSource.get();
    return {
      totalAssets: m.totalAssets,
      monthlyIncome: m.monthlyIncome,
      monthlyExpense: m.monthlyExpense,
      incomeChangeFromLastMonth: m.incomeChangeFromLastMonth,
      expenseChangeFromLastMonth: m.expenseChangeFromLastMonth,
      nextPaydayDays: m.nextPaydayDays,
      mainAccountBalance: m.mainAccountBalance,
      seedmoneyBalance: m.seedmoneyBalance,
    };
  }
}
