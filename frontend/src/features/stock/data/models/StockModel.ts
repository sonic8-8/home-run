export interface StockItemModel {
  stockCode: string
  stockName: string
  currentPrice: number
  pricePerShare: string
}

export interface StockMarketResponseModel {
  stocks: StockItemModel[]
}

export interface StockHoldingModel {
  stockCode: string
  stockName: string
  currentValue: number
  quantity: number
  avgPurchasePrice: number
  returnRate: number
}

export interface StockHoldingsResponseModel {
  totalValue: number
  totalReturnRate: number
  totalPurchaseAmount: number
  holdings: StockHoldingModel[]
}

export interface StockOrderRequestModel {
  stockCode: string
  orderType: string
  quantity: number
}

export interface StockOrderResponseModel {
  orderId: number
  stockCode: string
  orderType: string
  quantity: number
  pricePerShare: number
  totalAmount: number
  executeTurn: string
  orderStatus: string
}
