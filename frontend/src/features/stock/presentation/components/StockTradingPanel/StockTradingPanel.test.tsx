import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { StockTradingPanel } from './StockTradingPanel';
import { useStock } from '../../hooks/useStock';

vi.mock('../../hooks/useStock', () => ({
  useStock: vi.fn(),
}));

describe('StockTradingPanel', () => {
  it('renders market and holdings data and submits an order request', async () => {
    const order = vi.fn().mockResolvedValue({
      orderId: 41,
      stockCode: '005930',
      orderType: 'SELL',
      quantity: 3,
      pricePerShare: 75000,
      totalAmount: 225000,
      executeTurn: '턴 2',
      orderStatus: 'PENDING',
    });

    vi.mocked(useStock).mockReturnValue({
      market: {
        stocks: [
          {
            stockCode: '005930',
            stockName: '삼성전자',
            currentPrice: 75000,
            pricePerShare: '주당 75,000원',
          },
        ],
      },
      holdings: {
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
      },
      lastOrder: null,
      isLoading: false,
      isSubmittingOrder: false,
      marketError: null,
      holdingsError: null,
      orderError: null,
      refresh: vi.fn(),
      order,
    });

    render(<StockTradingPanel sessionId={7} />);

    expect(screen.getByRole('heading', { name: '주식 투자' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /삼성전자/ })).toBeInTheDocument();
    expect(screen.getAllByText('150,000 원')).not.toHaveLength(0);

    fireEvent.click(screen.getByRole('button', { name: '매도' }));
    fireEvent.change(screen.getByLabelText('주문 수량'), {
      target: { value: '3' },
    });
    fireEvent.click(screen.getByRole('button', { name: '주문 요청' }));

    await waitFor(() => {
      expect(order).toHaveBeenCalledWith({
        stockCode: '005930',
        orderType: 'SELL',
        quantity: 3,
      });
    });
  });

  it('shows loading, overview errors, and the last order result', () => {
    vi.mocked(useStock).mockReturnValue({
      market: {
        stocks: [
          {
            stockCode: '000660',
            stockName: 'SK하이닉스',
            currentPrice: 120000,
            pricePerShare: '주당 120,000원',
          },
        ],
      },
      holdings: {
        totalValue: 0,
        totalReturnRate: 0,
        totalPurchaseAmount: 0,
        holdings: [],
      },
      lastOrder: {
        orderId: 11,
        stockCode: '000660',
        orderType: 'BUY',
        quantity: 1,
        pricePerShare: 120000,
        totalAmount: 120000,
        executeTurn: '턴 2',
        orderStatus: 'PENDING',
      },
      isLoading: true,
      isSubmittingOrder: false,
      marketError: '시장 조회 실패',
      holdingsError: '보유 조회 실패',
      orderError: null,
      refresh: vi.fn(),
      order: vi.fn(),
    });

    render(<StockTradingPanel sessionId={7} />);

    expect(screen.getByRole('alert')).toHaveTextContent('시세 조회 실패: 시장 조회 실패');
    expect(screen.getByRole('alert')).toHaveTextContent('보유 현황 조회 실패: 보유 조회 실패');
    expect(screen.getByText('보유 현황을 불러오는 중입니다.')).toBeInTheDocument();
    expect(screen.getByTestId('stock-order-result')).toBeInTheDocument();
  });
});
