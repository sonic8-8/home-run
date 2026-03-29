import clsx from 'clsx';
import { useState, type FormEvent } from 'react';
import { formatMoney } from '@shared/utils/formatter';
import type {
  OrderStatus,
  OrderType,
  StockHolding,
} from '@features/stock/domain/entities/Stock';
import { useStock } from '../../hooks/useStock';
import styles from './StockTradingPanel.module.css';

const ORDER_TYPE_OPTIONS: { value: OrderType; label: string }[] = [
  { value: 'BUY', label: '매수' },
  { value: 'SELL', label: '매도' },
];

const ORDER_STATUS_LABEL: Record<OrderStatus, string> = {
  PENDING: '주문 접수',
  EXECUTED: '체결 완료',
  CANCELLED: '주문 취소',
};

interface StockTradingPanelProps {
  sessionId: number;
}

function getReturnRateClassName(returnRate: number): string {
  if (returnRate > 0) {
    return styles.positive;
  }

  if (returnRate < 0) {
    return styles.negative;
  }

  return styles.neutral;
}

function getSignedRateLabel(returnRate: number): string {
  if (returnRate > 0) {
    return `+${returnRate.toFixed(1)}%`;
  }

  if (returnRate < 0) {
    return `${returnRate.toFixed(1)}%`;
  }

  return '0.0%';
}

function getSelectedHolding(
  holdings: readonly StockHolding[],
  selectedStockCode: string | null,
): StockHolding | null {
  if (selectedStockCode === null) {
    return null;
  }

  return holdings.find((holding) => holding.stockCode === selectedStockCode) ?? null;
}

export function StockTradingPanel({
  sessionId,
}: StockTradingPanelProps) {
  const {
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
  } = useStock(sessionId);
  const [selectedStockCode, setSelectedStockCode] = useState<string | null>(null);
  const [orderType, setOrderType] = useState<OrderType>('BUY');
  const [quantityInput, setQuantityInput] = useState('1');

  const marketStocks = market?.stocks ?? [];
  const holdingItems = holdings?.holdings ?? [];
  const resolvedSelectedStockCode =
    selectedStockCode !== null &&
    marketStocks.some((stock) => stock.stockCode === selectedStockCode)
      ? selectedStockCode
      : marketStocks[0]?.stockCode ?? null;
  const selectedStock = marketStocks.find((stock) => stock.stockCode === resolvedSelectedStockCode) ?? null;
  const selectedHolding = getSelectedHolding(holdingItems, resolvedSelectedStockCode);
  const quantity = Number(quantityInput);
  const isQuantityValid = Number.isInteger(quantity) && quantity > 0;
  const hasOverviewError = marketError !== null || holdingsError !== null;

  const handleSubmitOrder = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (selectedStock === null || !isQuantityValid) {
      return;
    }

    const result = await order({
      stockCode: selectedStock.stockCode,
      orderType,
      quantity,
    });

    if (result !== null) {
      setQuantityInput('1');
    }
  };

  return (
    <section className={styles.panel} data-testid="stock-trading-panel">
      <header className={styles.header}>
        <div className={styles.headerBody}>
          <span className={styles.eyebrow}>Turn-Based Trading</span>
          <h1 className={styles.title}>주식 투자</h1>
          <p className={styles.description}>
            현재 턴의 시세와 보유 현황을 확인하고, 다음 턴에 반영될 주문을 접수합니다.
          </p>
        </div>
        <button
          type="button"
          className={styles.refreshButton}
          onClick={() => void refresh()}
          disabled={isLoading || isSubmittingOrder}
        >
          다시 조회
        </button>
      </header>

      {hasOverviewError && (
        <div className={styles.errorBanner} role="alert">
          {marketError && <p className={styles.errorText}>시세 조회 실패: {marketError}</p>}
          {holdingsError && <p className={styles.errorText}>보유 현황 조회 실패: {holdingsError}</p>}
        </div>
      )}

      <div className={styles.summaryGrid}>
        <article className={styles.summaryCard}>
          <span className={styles.summaryLabel}>총 평가 금액</span>
          <strong className={styles.summaryValue}>
            {holdings === null ? '-' : `${formatMoney(holdings.totalValue)} 원`}
          </strong>
        </article>
        <article className={styles.summaryCard}>
          <span className={styles.summaryLabel}>총 매입 금액</span>
          <strong className={styles.summaryValue}>
            {holdings === null ? '-' : `${formatMoney(holdings.totalPurchaseAmount)} 원`}
          </strong>
        </article>
        <article className={styles.summaryCard}>
          <span className={styles.summaryLabel}>총 수익률</span>
          <strong
            className={clsx(
              styles.summaryValue,
              holdings === null ? styles.neutral : getReturnRateClassName(holdings.totalReturnRate),
            )}
          >
            {holdings === null ? '-' : getSignedRateLabel(holdings.totalReturnRate)}
          </strong>
        </article>
      </div>

      <div className={styles.contentGrid}>
        <section className={styles.section}>
          <div className={styles.sectionHeader}>
            <h2 className={styles.sectionTitle}>시장 시세</h2>
            <span className={styles.sectionMeta}>총 {marketStocks.length}개 종목</span>
          </div>
          {isLoading && marketStocks.length === 0 ? (
            <div className={styles.stateCard}>시세를 불러오는 중입니다.</div>
          ) : marketStocks.length === 0 ? (
            <div className={styles.stateCard}>조회 가능한 종목이 없습니다.</div>
          ) : (
            <div className={styles.marketList}>
              {marketStocks.map((stock) => (
                <button
                  key={stock.stockCode}
                  type="button"
                  className={clsx(
                    styles.marketItem,
                    resolvedSelectedStockCode === stock.stockCode && styles.marketItemActive,
                  )}
                  onClick={() => setSelectedStockCode(stock.stockCode)}
                >
                  <div className={styles.marketItemTop}>
                    <strong className={styles.marketItemTitle}>{stock.stockName}</strong>
                    <span className={styles.marketItemCode}>{stock.stockCode}</span>
                  </div>
                  <div className={styles.marketItemBottom}>
                    <span className={styles.marketItemPrice}>{formatMoney(stock.currentPrice)} 원</span>
                    <span className={styles.marketItemMeta}>{stock.pricePerShare}</span>
                  </div>
                </button>
              ))}
            </div>
          )}
        </section>

        <section className={styles.section}>
          <div className={styles.sectionHeader}>
            <h2 className={styles.sectionTitle}>주문 입력</h2>
            <span className={styles.sectionMeta}>세션 #{sessionId}</span>
          </div>
          {selectedStock === null ? (
            <div className={styles.stateCard}>주문할 종목을 선택하면 상세 정보가 표시됩니다.</div>
          ) : (
            <>
              <article className={styles.detailCard}>
                <div className={styles.detailHeader}>
                  <div>
                    <strong className={styles.detailTitle}>{selectedStock.stockName}</strong>
                    <span className={styles.detailCode}>{selectedStock.stockCode}</span>
                  </div>
                  <strong className={styles.detailPrice}>
                    {formatMoney(selectedStock.currentPrice)} 원
                  </strong>
                </div>
                <div className={styles.detailMeta}>호가 기준 {selectedStock.pricePerShare}</div>
                <div className={styles.detailStats}>
                  <div className={styles.detailStat}>
                    <span className={styles.detailStatLabel}>보유 수량</span>
                    <strong className={styles.detailStatValue}>
                      {selectedHolding === null ? '0주' : `${formatMoney(selectedHolding.quantity)}주`}
                    </strong>
                  </div>
                  <div className={styles.detailStat}>
                    <span className={styles.detailStatLabel}>평균 매수가</span>
                    <strong className={styles.detailStatValue}>
                      {selectedHolding === null
                        ? '-'
                        : `${formatMoney(selectedHolding.avgPurchasePrice)} 원`}
                    </strong>
                  </div>
                </div>
              </article>

              <form className={styles.orderForm} onSubmit={(event) => void handleSubmitOrder(event)}>
                <div className={styles.toggleRow} role="group" aria-label="주문 유형">
                  {ORDER_TYPE_OPTIONS.map((option) => (
                    <button
                      key={option.value}
                      type="button"
                      className={clsx(
                        styles.toggleButton,
                        orderType === option.value && styles.toggleButtonActive,
                      )}
                      onClick={() => setOrderType(option.value)}
                    >
                      {option.label}
                    </button>
                  ))}
                </div>

                <label className={styles.fieldLabel} htmlFor="stock-order-quantity">
                  주문 수량
                </label>
                <input
                  id="stock-order-quantity"
                  className={styles.quantityInput}
                  type="number"
                  min={1}
                  inputMode="numeric"
                  value={quantityInput}
                  onChange={(event) => setQuantityInput(event.target.value)}
                />

                <button
                  type="submit"
                  className={styles.submitButton}
                  disabled={!isQuantityValid || isSubmittingOrder}
                >
                  {isSubmittingOrder ? '주문 접수 중...' : '주문 요청'}
                </button>
              </form>

              {orderError && (
                <div className={styles.errorBanner} role="alert">
                  <p className={styles.errorText}>주문 실패: {orderError}</p>
                </div>
              )}

              {lastOrder !== null && (
                <article className={styles.orderResult} data-testid="stock-order-result">
                  <strong className={styles.orderResultTitle}>주문 접수 완료</strong>
                  <p className={styles.orderResultText}>
                    {lastOrder.stockCode} {lastOrder.quantity.toLocaleString('ko-KR')}주 {lastOrder.orderType === 'BUY' ? '매수' : '매도'}
                  </p>
                  <p className={styles.orderResultText}>
                    총 주문 금액 {formatMoney(lastOrder.totalAmount)} 원 · {ORDER_STATUS_LABEL[lastOrder.orderStatus]}
                  </p>
                  <p className={styles.orderResultText}>체결 예정 턴 {lastOrder.executeTurn}</p>
                </article>
              )}
            </>
          )}
        </section>
      </div>

      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <h2 className={styles.sectionTitle}>보유 현황</h2>
          <span className={styles.sectionMeta}>총 {holdingItems.length}개 종목</span>
        </div>
        {isLoading && holdingItems.length === 0 ? (
          <div className={styles.stateCard}>보유 현황을 불러오는 중입니다.</div>
        ) : holdingItems.length === 0 ? (
          <div className={styles.stateCard}>보유 중인 주식이 없습니다.</div>
        ) : (
          <div className={styles.holdingsList}>
            {holdingItems.map((holding) => (
              <article key={holding.stockCode} className={styles.holdingItem}>
                <div className={styles.holdingHeader}>
                  <div>
                    <strong className={styles.holdingTitle}>{holding.stockName}</strong>
                    <span className={styles.holdingCode}>{holding.stockCode}</span>
                  </div>
                  <strong className={styles.holdingValue}>{formatMoney(holding.currentValue)} 원</strong>
                </div>
                <div className={styles.holdingStats}>
                  <span>{formatMoney(holding.quantity)}주 보유</span>
                  <span>평균 {formatMoney(holding.avgPurchasePrice)} 원</span>
                  <span className={getReturnRateClassName(holding.returnRate)}>
                    수익률 {getSignedRateLabel(holding.returnRate)}
                  </span>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </section>
  );
}

export default StockTradingPanel;
