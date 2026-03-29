import { useEffect, useState } from 'react';
import type { LoanCategory, LoanProduct } from '@features/loan/domain/entities/LoanProduct';
import { useLoan } from '@features/loan/presentation/hooks/useLoan';
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
  preSelectedPropertyId?: string;
  preSelectedPropertyName?: string;
  preSelectedPropertyPrice?: number;
}

export function LoanProductsPanel({
  sessionId,
  preSelectedPropertyId,
  preSelectedPropertyName,
  preSelectedPropertyPrice,
}: LoanProductsPanelProps) {
  const { productsPage, loading, error, fetchProducts } = useLoan(sessionId);
  const [activeFilter, setActiveFilter] = useState<FilterType>('all');
  const [page, setPage] = useState(1);
  const [selected, setSelected] = useState<LoanProduct | null>(null);

  useEffect(() => {
    void fetchProducts(FILTER_TO_CATEGORY[activeFilter], 0, PRODUCT_FETCH_SIZE);
  }, [activeFilter, fetchProducts]);

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

  return (
    <div className={styles.panel}>
      <h2 className={styles.title}>대출 상품</h2>

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
              <span className={styles.icon}>{product.bankName.charAt(0)}</span>
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
