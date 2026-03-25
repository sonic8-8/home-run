import type { Pass } from '../entities/Pass';
import type { PassSubscription } from '../entities/PassSubscription';
import type { PassSubscribeResult } from '../entities/PassSubscribeResult';
import type { PassSaveResult } from '../entities/PassSaveResult';
import type { PassWidgetData } from '../entities/PassWidgetData';
import type { PassHistoryPage } from '../entities/PassHistory';

export interface IPassRepository {
  getProducts(): Promise<Pass[]>;
  getSubscriptions(): Promise<PassSubscription[]>;
  subscribe(passId: number, sourceAccountId: string): Promise<PassSubscribeResult>;
  unsubscribe(subscriptionId: number): Promise<void>;
  save(subscriptionId: number, sourceAccountId: string): Promise<PassSaveResult>;
  getWidget(): Promise<PassWidgetData>;
  getHistory(page: number, size: number): Promise<PassHistoryPage>;
}
