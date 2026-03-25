import type { IPassRepository } from '../../domain/repositories/IPassRepository';
import type { Pass } from '../../domain/entities/Pass';
import type { PassSubscription } from '../../domain/entities/PassSubscription';
import type { PassSubscribeResult } from '../../domain/entities/PassSubscribeResult';
import type { PassSaveResult } from '../../domain/entities/PassSaveResult';
import type { PassWidgetData } from '../../domain/entities/PassWidgetData';
import type { PassHistoryPage } from '../../domain/entities/PassHistory';
import { PassRemoteDataSource } from '../datasources/PassRemoteDataSource';

export class PassRepositoryImpl implements IPassRepository {
  private readonly dataSource: PassRemoteDataSource;
  constructor(dataSource: PassRemoteDataSource) { this.dataSource = dataSource; }

  async getProducts(): Promise<Pass[]> {
    const m = await this.dataSource.getProducts();
    return m.products.map((p) => ({
      passId: p.passId,
      name: p.name,
      description: p.description,
      amountPerSave: p.amountPerSave,
    }));
  }

  async getSubscriptions(): Promise<PassSubscription[]> {
    const m = await this.dataSource.getSubscriptions();
    return m.subscriptions.map((s) => ({
      subscriptionId: s.subscriptionId,
      passId: s.passId,
      name: s.name,
      amountPerSave: s.amountPerSave,
      totalSaved: s.totalSaved,
      weeklyHistory: s.weeklyHistory,
    }));
  }

  async subscribe(passId: number): Promise<PassSubscribeResult> {
    const m = await this.dataSource.subscribe(passId);
    return {
      subscriptionId: m.subscriptionId,
      passId: m.passId,
      name: m.name,
      subscribedAt: m.subscribedAt,
    };
  }

  unsubscribe(subscriptionId: number): Promise<void> {
    return this.dataSource.unsubscribe(subscriptionId);
  }

  async save(subscriptionId: number): Promise<PassSaveResult> {
    const m = await this.dataSource.save(subscriptionId);
    return { savedAmount: m.savedAmount, totalSaved: m.totalSaved, remainingBalance: m.remainingBalance };
  }

  async getWidget(): Promise<PassWidgetData> {
    const m = await this.dataSource.getWidget();
    return {
      todaySaved: m.todaySaved,
      weeklySaved: m.weeklySaved,
      weeklyGoal: m.weeklyGoal,
      progressRate: m.progressRate,
      remaining: m.remaining,
    };
  }

  async getHistory(page: number, size: number): Promise<PassHistoryPage> {
    const m = await this.dataSource.getHistory(page, size);
    return {
      content: m.content.map((item) => ({
        historyId: item.historyId,
        passName: item.passName,
        amount: item.amount,
        savedAt: item.savedAt,
      })),
      page: m.page,
      size: m.size,
      totalElements: m.totalElements,
      totalPages: m.totalPages,
    };
  }
}
