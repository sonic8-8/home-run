import type { ICreditScoreRepository } from '../../domain/repositories/ICreditScoreRepository';
import type { CreditScore } from '../../domain/entities/CreditScore';
import { CreditScoreRemoteDataSource } from '../datasources/CreditScoreRemoteDataSource';

export class CreditScoreRepositoryImpl implements ICreditScoreRepository {
  private readonly dataSource: CreditScoreRemoteDataSource;
  constructor(dataSource: CreditScoreRemoteDataSource) { this.dataSource = dataSource; }

  async get(): Promise<CreditScore> {
    const m = await this.dataSource.get();
    return {
      score: m.score,
      grade: m.grade,
      gradeLabel: m.gradeLabel,
      paymentHistory: m.paymentHistory,
      amountsOwed: m.amountsOwed,
      creditLength: m.creditLength,
      creditMix: m.creditMix,
      newCredit: m.newCredit,
      ratingName: m.ratingName,
      totalAsset: m.totalAsset,
      totalDebt: m.totalDebt,
      netAsset: m.netAsset,
    };
  }
}
