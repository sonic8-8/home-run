import { useEffect, useState } from 'react';
import type { LoanConfirmResult } from '@features/loan/domain/entities/ActiveLoan';
import type { LoanCategory, LoanProduct } from '@features/loan/domain/entities/LoanProduct';
import { useLoan } from '@features/loan/presentation/hooks/useLoan';
import {
  readActiveLoan,
  type StoredActiveLoan,
  toStoredActiveLoan,
  writeActiveLoan,
} from '@features/loan/presentation/activeLoanStorage';
import { AuthImage } from '@shared/components/AuthImage/AuthImage';
import { LoanDetailPanel } from './LoanDetailPanel';
import styles from './LoanProductsPanel.module.css';

const PAGE_SIZE = 6;
const PRODUCT_FETCH_SIZE = 200;

type FilterType = 'all' | 'credit' | 'mortgage' | 'recommended';

const FILTERS: { key: FilterType; label: string }[] = [
  { key: 'all',         label: '전체보기' },
  { key: 'credit',      label: '신용' },
  { key: 'mortgage',    label: '주택 담보 대출' },
  { key: 'recommended', label: '당신을 위한 추천대출' },
];

const FILTER_TO_CATEGORY: Record<FilterType, LoanCategory> = {
  all: 'ALL',
  credit: 'CREDIT',
  mortgage: 'MORTGAGE',
  recommended: 'ALL',
};

const LOAN_TYPE_LABEL: Record<LoanCategory, string> = {
  ALL: '전체',
  CREDIT: '신용대출',
  JEONSE: '전세자금대출',
  MORTGAGE: '주택담보대출',
};

function filterProducts(products: LoanProduct[], filter: FilterType): LoanProduct[] {
  if (filter === 'credit') {
    return products.filter((p) => p.productType === 'CREDIT');
  }

  if (filter === 'mortgage') {
    return products.filter((p) => p.productType === 'MORTGAGE');
  }

  if (filter === 'recommended') {
    return products.slice(0, 3);
  }

  return products;
}

interface LoanProductsPanelProps {
  sessionId: number;
  confirmedLoan?: LoanConfirmResult;
  preSelectedPropertyId?: string;
  preSelectedPropertyName?: string;
  preSelectedPropertyPrice?: number;
}

export function LoanProductsPanel({
  sessionId,
  confirmedLoan,
  preSelectedPropertyId,
  preSelectedPropertyName,
  preSelectedPropertyPrice,
}: LoanProductsPanelProps) {
  const { productsPage, loading, error, fetchProducts, repay } = useLoan(sessionId);
  const [activeFilter, setActiveFilter] = useState<FilterType>('all');
  const [page, setPage] = useState(1);
  const [selected, setSelected] = useState<LoanProduct | null>(null);
  const [activeLoan, setActiveLoan] = useState<StoredActiveLoan | null>(() =>
    confirmedLoan !== undefined ? toStoredActiveLoan(confirmedLoan) : readActiveLoan(sessionId),
  );
  const [repayAmount, setRepayAmount] = useState('');
  const [repayError, setRepayError] = useState<string | null>(null);
  const [isRepaying, setIsRepaying] = useState(false);

  useEffect(() => {
    void fetchProducts(FILTER_TO_CATEGORY[activeFilter], 0, PRODUCT_FETCH_SIZE);
  }, [activeFilter, fetchProducts]);

  useEffect(() => {
    if (confirmedLoan === undefined) {
      return;
    }

    writeActiveLoan(sessionId, toStoredActiveLoan(confirmedLoan));
  }, [confirmedLoan, sessionId]);

  // 상세 화면
  if (selected) {
    return (
      <LoanDetailPanel
        product={selected}
        sessionId={sessionId}
        onBack={() => setSelected(null)}
        preSelectedPropertyId={preSelectedPropertyId}
        preSelectedPropertyName={preSelectedPropertyName}
        preSelectedPropertyPrice={preSelectedPropertyPrice}
      />
    );
  }

  const filtered = filterProducts(productsPage?.content ?? [], activeFilter);
  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const paged = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  const handleFilter = (f: FilterType) => {
    setActiveFilter(f);
    setPage(1);
  };

  const handleRepay = async () => {
    if (activeLoan === null || activeLoan.status === 'CLOSED') {
      return;
    }

    const amount = Number(repayAmount.replace(/,/g, ''));
    if (!Number.isFinite(amount) || amount <= 0) {
      setRepayError('상환 금액을 올바르게 입력해 주세요.');
      return;
    }

    if (amount > activeLoan.remainingPrincipal) {
      setRepayError('남은 원금 이하로 입력해 주세요.');
      return;
    }

    setRepayError(null);
    setIsRepaying(true);

    try {
      const result = await repay(activeLoan.loanId, amount);
      if (result === null) {
        return;
      }

      const nextLoan: StoredActiveLoan =
        result.remainingPrincipal <= 0
          ? {
              ...activeLoan,
              remainingPrincipal: 0,
              monthlyPayment: result.updatedMonthlyPayment,
              status: 'CLOSED',
            }
          : {
              ...activeLoan,
              remainingPrincipal: result.remainingPrincipal,
              monthlyPayment: result.updatedMonthlyPayment,
            };

      const nextActiveLoan = nextLoan.status === 'CLOSED' ? null : nextLoan;
      setActiveLoan(nextActiveLoan);
      writeActiveLoan(sessionId, nextActiveLoan);
      setRepayAmount('');
    } catch {
      setRepayError('대출 상환 처리에 실패했습니다.');
    } finally {
      setIsRepaying(false);
    }
  };

  return (
    <div className={styles.panel} data-guide="game-loan-panel">
      <h2 className={styles.title}>대출/상환</h2>

      {activeLoan !== null && (
        <section className={styles.activeLoanCard} data-testid="active-loan-card">
          <div className={styles.activeLoanHeader}>
            <div>
              <div className={styles.activeLoanTitle}>확정된 대출</div>
              <div className={styles.activeLoanMeta}>
                부동산 계약에 사용한 대출입니다. 원할 때 중도 상환할 수 있습니다.
              </div>
            </div>
            <div className={styles.activeLoanRate}>{activeLoan.annualRate.toFixed(2)}%</div>
          </div>
          <div className={styles.activeLoanStats}>
            <div>
              <span className={styles.activeLoanLabel}>남은 원금</span>
              <strong className={styles.activeLoanValue}>
                {activeLoan.remainingPrincipal.toLocaleString('ko-KR')}원
              </strong>
            </div>
            <div>
              <span className={styles.activeLoanLabel}>월 납입금</span>
              <strong className={styles.activeLoanValue}>
                {activeLoan.monthlyPayment.toLocaleString('ko-KR')}원
              </strong>
            </div>
          </div>
          <div className={styles.repayRow}>
            <input
              className={styles.repayInput}
              type="text"
              inputMode="numeric"
              placeholder="상환할 금액 입력"
              value={repayAmount}
              onChange={(event) => {
                const digits = event.target.value.replace(/[^0-9]/g, '');
                setRepayAmount(digits ? Number(digits).toLocaleString('ko-KR') : '');
              }}
            />
            <button
              type="button"
              className={styles.repayButton}
              onClick={() => {
                void handleRepay();
              }}
              disabled={isRepaying}
            >
              {isRepaying ? '상환 중...' : '상환하기'}
            </button>
          </div>
          {repayError !== null && (
            <div className={styles.repayError} role="alert">{repayError}</div>
          )}
        </section>
      )}

      {/* 필터 탭 */}
      <div className={styles.filters}>
        {FILTERS.map(({ key, label }) => (
          <button
            key={key}
            className={activeFilter === key ? styles.filterActive : styles.filter}
            onClick={() => handleFilter(key)}
          >
            {label}
          </button>
        ))}
      </div>

      {/* 상품 목록 */}
      {loading && filtered.length === 0 ? (
        <div className={styles.statusMessage}>대출 상품을 불러오는 중입니다.</div>
      ) : error && filtered.length === 0 ? (
        <div role="alert" className={styles.statusMessage}>{error}</div>
      ) : paged.length === 0 ? (
        <div className={styles.statusMessage}>조건에 맞는 대출 상품이 없습니다.</div>
      ) : (
        <div className={styles.list}>
          {paged.map((product) => (
            <button
              key={product.productId}
              type="button"
              className={styles.item}
              onClick={() => setSelected(product)}
            >
              <div className={styles.icon}>
                <AuthImage
                  src={product.bankLogoUrl}
                  alt={product.bankName}
                  className={styles.logoImage}
                  fallback={<span className={styles.logoFallback}>{product.bankName.charAt(0)}</span>}
                />
              </div>
              <div className={styles.info}>
                <span className={styles.productName}>{product.productName}</span>
                <span className={styles.loanType}>{LOAN_TYPE_LABEL[product.productType]}</span>
              </div>
              <div className={styles.rate}>
                <span className={styles.rateLabel}>연이율</span>
                <span className={styles.rateMain}>{product.minRate.toFixed(2)} %</span>
                <span className={styles.rateMax}>~ {product.maxRate.toFixed(2)}%</span>
              </div>
            </button>
          ))}
        </div>
      )}

      {/* 페이지네이션 */}
      {totalPages > 1 && (
        <div className={styles.pagination}>
          <button className={styles.pageBtn} onClick={() => setPage(1)} disabled={page === 1}>« First</button>
          <button className={styles.pageBtn} onClick={() => setPage((p) => Math.max(1, p - 1))} disabled={page === 1}>‹ Back</button>
          {Array.from({ length: totalPages }, (_, i) => i + 1).map((p) => (
            <button
              key={p}
              className={p === page ? styles.pageBtnActive : styles.pageBtn}
              onClick={() => setPage(p)}
            >
              {p}
            </button>
          ))}
          <button className={styles.pageBtn} onClick={() => setPage((p) => Math.min(totalPages, p + 1))} disabled={page === totalPages}>Next ›</button>
          <button className={styles.pageBtn} onClick={() => setPage(totalPages)} disabled={page === totalPages}>Last »</button>
        </div>
      )}
    </div>
  );
}

export default LoanProductsPanel;
