import type { IStockRepository } from '@features/stock/domain/repositories/IStockRepository'
import type { StockOrder, StockOrderParams } from '@features/stock/domain/entities/Stock'

export class OrderStockUseCase {
  private readonly repository: IStockRepository

  constructor(repository: IStockRepository) {
    this.repository = repository
  }

  async execute(sessionId: number, params: StockOrderParams): Promise<StockOrder> {
    return this.repository.order(sessionId, params)
  }
}
