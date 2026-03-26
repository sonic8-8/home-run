import type { StockMarket, StockHoldings, StockOrder, StockOrderParams } from '@features/stock/domain/entities/Stock'

export interface IStockRepository {
  getMarket(sessionId: number): Promise<StockMarket>
  getHoldings(sessionId: number): Promise<StockHoldings>
  order(sessionId: number, params: StockOrderParams): Promise<StockOrder>
}
