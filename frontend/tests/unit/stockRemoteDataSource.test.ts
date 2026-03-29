import { beforeEach, describe, expect, it, vi } from 'vitest';

const { get, post } = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
}));

vi.mock('@core/network/apiClient', () => ({
  apiClient: {
    get,
    post,
  },
}));

import { StockRemoteDataSource } from '@features/stock/data/datasources/StockRemoteDataSource';

describe('StockRemoteDataSource', () => {
  beforeEach(() => {
    get.mockReset();
    post.mockReset();
  });

  it('uses the stock market endpoint', async () => {
    get.mockResolvedValue({ data: { data: { stocks: [] } } });
    const dataSource = new StockRemoteDataSource();

    await dataSource.getMarket(7);

    expect(get).toHaveBeenCalledWith('/games/sessions/7/stocks/market');
  });

  it('uses the stock holdings endpoint', async () => {
    get.mockResolvedValue({ data: { data: { holdings: [] } } });
    const dataSource = new StockRemoteDataSource();

    await dataSource.getHoldings(7);

    expect(get).toHaveBeenCalledWith('/games/sessions/7/stocks/holdings');
  });

  it('uses the stock order endpoint', async () => {
    post.mockResolvedValue({ data: { data: { orderId: 1 } } });
    const dataSource = new StockRemoteDataSource();

    await dataSource.order(7, {
      stockCode: '005930',
      orderType: 'BUY',
      quantity: 2,
    });

    expect(post).toHaveBeenCalledWith('/games/sessions/7/stocks/orders', {
      stockCode: '005930',
      orderType: 'BUY',
      quantity: 2,
    });
  });
});
