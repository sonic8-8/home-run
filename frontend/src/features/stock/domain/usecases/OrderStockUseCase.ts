import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { IStockRepository } from '@features/stock/domain/repositories/IStockRepository'
import type { StockOrder, StockOrderParams } from '@features/stock/domain/entities/Stock'

@injectable()
export class OrderStockUseCase {
  private readonly repository: IStockRepository

  constructor(
    @inject(DI_TOKENS.IStockRepository)
    repository: IStockRepository,
  ) {
    this.repository = repository
  }

  async execute(sessionId: number, params: StockOrderParams): Promise<StockOrder> {
    return this.repository.order(sessionId, params)
  }
}
