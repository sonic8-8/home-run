import { useCallback, useEffect, useState } from 'react';
import { container } from '@core/di/container';
import { toErrorMessage } from '@core/error/AppError';
import { GetStockMarketUseCase } from '@features/stock/domain/usecases/GetStockMarketUseCase'
import { GetStockHoldingsUseCase } from '@features/stock/domain/usecases/GetStockHoldingsUseCase'
import { OrderStockUseCase } from '@features/stock/domain/usecases/OrderStockUseCase'
import type { StockMarket, StockHoldings, StockOrder, StockOrderParams } from '@features/stock/domain/entities/Stock'

export function useStock(sessionId: number) {
  const [market, setMarket] = useState<StockMarket | null>(null);
  const [holdings, setHoldings] = useState<StockHoldings | null>(null);
  const [lastOrder, setLastOrder] = useState<StockOrder | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmittingOrder, setIsSubmittingOrder] = useState(false);
  const [marketError, setMarketError] = useState<string | null>(null);
  const [holdingsError, setHoldingsError] = useState<string | null>(null);
  const [orderError, setOrderError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    setIsLoading(true);
    setMarketError(null);
    setHoldingsError(null);

    const getMarketUseCase = container.resolve(GetStockMarketUseCase);
    const getHoldingsUseCase = container.resolve(GetStockHoldingsUseCase);
    const [marketResult, holdingsResult] = await Promise.allSettled([
      getMarketUseCase.execute(sessionId),
      getHoldingsUseCase.execute(sessionId),
    ]);

    if (marketResult.status === 'fulfilled') {
      setMarket(marketResult.value);
    } else {
      setMarketError(toErrorMessage(marketResult.reason));
    }

    if (holdingsResult.status === 'fulfilled') {
      setHoldings(holdingsResult.value);
    } else {
      setHoldingsError(toErrorMessage(holdingsResult.reason));
    }

    setIsLoading(false);
  }, [sessionId]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const order = useCallback(
    async (params: StockOrderParams): Promise<StockOrder | null> => {
      setIsSubmittingOrder(true);
      setOrderError(null);
      try {
        const orderUseCase = container.resolve(OrderStockUseCase);
        const result = await orderUseCase.execute(sessionId, params);
        setLastOrder(result);
        await refresh();
        return result;
      } catch (error) {
        setOrderError(toErrorMessage(error));
        return null;
      } finally {
        setIsSubmittingOrder(false);
      }
    },
    [refresh, sessionId],
  );

  return {
    market,
    holdings,
    lastOrder,
    isLoading,
    isSubmittingOrder,
    marketError,
    holdingsError,
    orderError,
    refresh,
    order,
  };
}
