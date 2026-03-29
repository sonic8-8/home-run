import { describe, expect, it, vi } from 'vitest';
import type { StockRemoteDataSource } from '../datasources/StockRemoteDataSource';
import { StockRepositoryImpl } from './StockRepositoryImpl';

describe('StockRepositoryImpl', () => {
  it('maps the stock market response to entities', async () => {
    const dataSource = {
      getMarket: vi.fn().mockResolvedValue({
        stocks: [
          {
            stockCode: '005930',
            stockName: '삼성전자',
            currentPrice: 75000,
            pricePerShare: '주당 75,000원',
          },
        ],
      }),
      getHoldings: vi.fn(),
      order: vi.fn(),
    } as Pick<StockRemoteDataSource, 'getMarket' | 'getHoldings' | 'order'> as StockRemoteDataSource;
    const repository = new StockRepositoryImpl(dataSource);

    await expect(repository.getMarket(7)).resolves.toEqual({
      stocks: [
        {
          stockCode: '005930',
          stockName: '삼성전자',
          currentPrice: 75000,
          pricePerShare: '주당 75,000원',
        },
      ],
    });
  });

  it('maps the holdings and order responses to entities', async () => {
    const dataSource = {
      getMarket: vi.fn(),
      getHoldings: vi.fn().mockResolvedValue({
        totalValue: 150000,
        totalReturnRate: 7.1,
        totalPurchaseAmount: 140000,
        holdings: [
          {
            stockCode: '005930',
            stockName: '삼성전자',
            currentValue: 150000,
            quantity: 2,
            avgPurchasePrice: 70000,
            returnRate: 7.1,
          },
        ],
      }),
      order: vi.fn().mockResolvedValue({
        orderId: 12,
        stockCode: '005930',
        orderType: 'BUY',
        quantity: 2,
        pricePerShare: 75000,
        totalAmount: 150000,
        executeTurn: '턴 2',
        orderStatus: 'PENDING',
      }),
    } as Pick<StockRemoteDataSource, 'getMarket' | 'getHoldings' | 'order'> as StockRemoteDataSource;
    const repository = new StockRepositoryImpl(dataSource);

    await expect(repository.getHoldings(7)).resolves.toEqual({
      totalValue: 150000,
      totalReturnRate: 7.1,
      totalPurchaseAmount: 140000,
      holdings: [
        {
          stockCode: '005930',
          stockName: '삼성전자',
          currentValue: 150000,
          quantity: 2,
          avgPurchasePrice: 70000,
          returnRate: 7.1,
        },
      ],
    });

    await expect(
      repository.order(7, {
        stockCode: '005930',
        orderType: 'BUY',
        quantity: 2,
      }),
    ).resolves.toEqual({
      orderId: 12,
      stockCode: '005930',
      orderType: 'BUY',
      quantity: 2,
      pricePerShare: 75000,
      totalAmount: 150000,
      executeTurn: '턴 2',
      orderStatus: 'PENDING',
    });
  });
});
