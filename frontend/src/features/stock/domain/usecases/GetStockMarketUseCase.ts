import type { IStockRepository } from '@features/stock/domain/repositories/IStockRepository'
import type { StockMarket } from '@features/stock/domain/entities/Stock'

export class GetStockMarketUseCase {
  private readonly repository: IStockRepository

  constructor(repository: IStockRepository) {
    this.repository = repository
  }

  async execute(sessionId: number): Promise<StockMarket> {
    return this.repository.getMarket(sessionId)
  }
}
