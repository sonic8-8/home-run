import { useState, useCallback } from 'react'
import { StockRemoteDataSource } from '@features/stock/data/datasources/StockRemoteDataSource'
import { StockRepositoryImpl } from '@features/stock/data/repositories/StockRepositoryImpl'
import { GetStockMarketUseCase } from '@features/stock/domain/usecases/GetStockMarketUseCase'
import { GetStockHoldingsUseCase } from '@features/stock/domain/usecases/GetStockHoldingsUseCase'
import { OrderStockUseCase } from '@features/stock/domain/usecases/OrderStockUseCase'
import type { StockMarket, StockHoldings, StockOrder, StockOrderParams } from '@features/stock/domain/entities/Stock'

const stockRepo = new StockRepositoryImpl(new StockRemoteDataSource())
const getMarketUseCase = new GetStockMarketUseCase(stockRepo)
const getHoldingsUseCase = new GetStockHoldingsUseCase(stockRepo)
const orderUseCase = new OrderStockUseCase(stockRepo)

export function useStock(sessionId: number) {
  const [market, setMarket] = useState<StockMarket | null>(null)
  const [holdings, setHoldings] = useState<StockHoldings | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchMarket = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const result = await getMarketUseCase.execute(sessionId)
      setMarket(result)
      return result
    } catch (e) {
      setError(e instanceof Error ? e.message : '주식 시장 정보를 불러오지 못했습니다.')
      return null
    } finally {
      setLoading(false)
    }
  }, [sessionId])

  const fetchHoldings = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const result = await getHoldingsUseCase.execute(sessionId)
      setHoldings(result)
      return result
    } catch (e) {
      setError(e instanceof Error ? e.message : '보유 주식 정보를 불러오지 못했습니다.')
      return null
    } finally {
      setLoading(false)
    }
  }, [sessionId])

  const order = useCallback(
    async (params: StockOrderParams): Promise<StockOrder | null> => {
      setLoading(true)
      setError(null)
      try {
        return await orderUseCase.execute(sessionId, params)
      } catch (e) {
        setError(e instanceof Error ? e.message : '주식 주문에 실패했습니다.')
        return null
      } finally {
        setLoading(false)
      }
    },
    [sessionId],
  )

  return { market, holdings, loading, error, fetchMarket, fetchHoldings, order }
}
