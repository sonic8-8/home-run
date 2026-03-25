import type { CreditScore } from '../entities/CreditScore';

export interface ICreditScoreRepository {
  get(): Promise<CreditScore>;
}
