import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { IStockRepository } from '@features/stock/domain/repositories/IStockRepository'
import type { StockHoldings } from '@features/stock/domain/entities/Stock'

@injectable()
export class GetStockHoldingsUseCase {
  private readonly repository: IStockRepository

  constructor(
    @inject(DI_TOKENS.IStockRepository)
    repository: IStockRepository,
  ) {
    this.repository = repository
  }

  async execute(sessionId: number): Promise<StockHoldings> {
    return this.repository.getHoldings(sessionId)
  }
}
