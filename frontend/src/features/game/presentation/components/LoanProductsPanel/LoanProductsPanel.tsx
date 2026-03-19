import { useState } from 'react';
import { LoanDetailPanel } from './LoanDetailPanel';
import styles from './LoanProductsPanel.module.css';

interface LoanProduct {
  productId: number;
  bankName: string;
  productName: string;
  loanType: string;
  minRate: number;
  maxRate: number;
  icon: string;
}

// TODO: GET /games/sessions/{id}/loans/products
const MOCK_PRODUCTS: LoanProduct[] = [
  { productId: 1, bankName: 'NH농협은행', productName: 'NH 주택담보 대출',  loanType: '주택담보대출', minRate: 3.49, maxRate: 5.89, icon: '🌾' },
  { productId: 2, bankName: '신한은행',   productName: '신한 주택담보 대출', loanType: '주택담보대출', minRate: 3.49, maxRate: 5.89, icon: '🔴' },
  { productId: 3, bankName: '우리은행',   productName: '우리 주택담보 대출', loanType: '주택담보대출', minRate: 3.49, maxRate: 5.89, icon: '🌊' },
  { productId: 4, bankName: 'KB국민은행', productName: 'KB 주택담보 대출',  loanType: '주택담보대출', minRate: 3.49, maxRate: 5.89, icon: '⭐' },
  { productId: 5, bankName: '카카오뱅크', productName: '카카오 신용대출',    loanType: '신용대출',     minRate: 4.10, maxRate: 6.50, icon: '💛' },
  { productId: 6, bankName: '토스뱅크',   productName: '토스 신용대출',      loanType: '신용대출',     minRate: 4.50, maxRate: 7.00, icon: '🔵' },
];

const PAGE_SIZE = 6;

type FilterType = 'all' | 'credit' | 'mortgage' | 'recommended';

const FILTERS: { key: FilterType; label: string }[] = [
  { key: 'all',         label: '전체보기' },
  { key: 'credit',      label: '신용' },
  { key: 'mortgage',    label: '주택 담보 대출' },
  { key: 'recommended', label: '당신을 위한 추천대출' },
];

function filterProducts(products: LoanProduct[], filter: FilterType): LoanProduct[] {
  if (filter === 'credit')   return products.filter((p) => p.loanType === '신용대출');
  if (filter === 'mortgage') return products.filter((p) => p.loanType === '주택담보대출');
  return products;
}

interface LoanProductsPanelProps {
  sessionId: number;
  preSelectedPropertyId?: string;
  preSelectedPropertyName?: string;
  preSelectedPropertyPrice?: number;
  onApplyDirect?: (propertyId: string, propertyName: string, propertyPrice: number) => void;
}

export function LoanProductsPanel({
  sessionId,
  preSelectedPropertyId,
  preSelectedPropertyName,
  preSelectedPropertyPrice,
  onApplyDirect,
}: LoanProductsPanelProps) {
  const [activeFilter, setActiveFilter] = useState<FilterType>('all');
  const [page, setPage] = useState(1);
  const [selected, setSelected] = useState<LoanProduct | null>(null);

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
        onApplyDirect={onApplyDirect}
      />
    );
  }

  const filtered = filterProducts(MOCK_PRODUCTS, activeFilter);
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
      <div className={styles.list}>
        {paged.map((product) => (
          <div
            key={product.productId}
            className={styles.item}
            onClick={() => setSelected(product)}
          >
            <span className={styles.icon}>{product.icon}</span>
            <div className={styles.info}>
              <span className={styles.productName}>{product.productName}</span>
              <span className={styles.loanType}>{product.loanType}</span>
            </div>
            <div className={styles.rate}>
              <span className={styles.rateLabel}>연이율</span>
              <span className={styles.rateMain}>{product.minRate.toFixed(2)} %</span>
              <span className={styles.rateMax}>~ {product.maxRate.toFixed(2)}%</span>
            </div>
          </div>
        ))}
      </div>

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
