export type OrderType = 'BUY' | 'SELL'
export type OrderStatus = 'PENDING' | 'EXECUTED' | 'CANCELLED'

export interface StockItem {
  readonly stockCode: string
  readonly stockName: string
  readonly currentPrice: number
  readonly pricePerShare: string
}

export interface StockMarket {
  readonly stocks: StockItem[]
}

export interface StockHolding {
  readonly stockCode: string
  readonly stockName: string
  readonly currentValue: number
  readonly quantity: number
  readonly avgPurchasePrice: number
  readonly returnRate: number
}

export interface StockHoldings {
  readonly totalValue: number
  readonly totalReturnRate: number
  readonly totalPurchaseAmount: number
  readonly holdings: StockHolding[]
}

export interface StockOrderParams {
  readonly stockCode: string
  readonly orderType: OrderType
  readonly quantity: number
}

export interface StockOrder {
  readonly orderId: number
  readonly stockCode: string
  readonly orderType: OrderType
  readonly quantity: number
  readonly pricePerShare: number
  readonly totalAmount: number
  readonly executeTurn: string
  readonly orderStatus: OrderStatus
}
