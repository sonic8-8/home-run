import { inject, injectable } from 'tsyringe';
import type { IStockRepository } from '@features/stock/domain/repositories/IStockRepository'
import type {
  StockMarket,
  StockHoldings,
  StockOrder,
  StockOrderParams,
  OrderType,
  OrderStatus,
} from '@features/stock/domain/entities/Stock'
import { StockRemoteDataSource } from '@features/stock/data/datasources/StockRemoteDataSource'

@injectable()
export class StockRepositoryImpl implements IStockRepository {
  private readonly dataSource: StockRemoteDataSource

  constructor(
    @inject(StockRemoteDataSource)
    dataSource: StockRemoteDataSource,
  ) {
    this.dataSource = dataSource
  }

  async getMarket(sessionId: number): Promise<StockMarket> {
    const model = await this.dataSource.getMarket(sessionId)
    return {
      stocks: model.stocks.map((s) => ({
        stockCode: s.stockCode,
        stockName: s.stockName,
        currentPrice: s.currentPrice,
        pricePerShare: s.pricePerShare,
      })),
    }
  }

  async getHoldings(sessionId: number): Promise<StockHoldings> {
    const model = await this.dataSource.getHoldings(sessionId)
    return {
      totalValue: model.totalValue,
      totalReturnRate: model.totalReturnRate,
      totalPurchaseAmount: model.totalPurchaseAmount,
      holdings: model.holdings.map((h) => ({
        stockCode: h.stockCode,
        stockName: h.stockName,
        currentValue: h.currentValue,
        quantity: h.quantity,
        avgPurchasePrice: h.avgPurchasePrice,
        returnRate: h.returnRate,
      })),
    }
  }

  async order(sessionId: number, params: StockOrderParams): Promise<StockOrder> {
    const model = await this.dataSource.order(sessionId, {
      stockCode: params.stockCode,
      orderType: params.orderType,
      quantity: params.quantity,
    })
    return {
      orderId: model.orderId,
      stockCode: model.stockCode,
      orderType: model.orderType as OrderType,
      quantity: model.quantity,
      pricePerShare: model.pricePerShare,
      totalAmount: model.totalAmount,
      executeTurn: model.executeTurn,
      orderStatus: model.orderStatus as OrderStatus,
    }
  }
}
