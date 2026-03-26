import type { IStockRepository } from '@features/stock/domain/repositories/IStockRepository'
import type { StockHoldings } from '@features/stock/domain/entities/Stock'

export class GetStockHoldingsUseCase {
  private readonly repository: IStockRepository

  constructor(repository: IStockRepository) {
    this.repository = repository
  }

  async execute(sessionId: number): Promise<StockHoldings> {
    return this.repository.getHoldings(sessionId)
  }
}
