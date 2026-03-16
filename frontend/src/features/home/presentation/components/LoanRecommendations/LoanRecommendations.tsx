import React from 'react';
import type { LoanRecommendation } from '../../../domain/entities/LoanRecommendation';
import styles from './LoanRecommendations.module.css';

interface LoanRecommendationsProps {
  loans: LoanRecommendation[];
}

const BANK_COLORS: Record<string, string> = {
  NH: '#facc15',
  KB: '#f97316',
  KEB: '#22c55e',
};

export const LoanRecommendations: React.FC<LoanRecommendationsProps> = ({ loans }) => {
  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <span className={styles.icon}>🏦</span>
        <div>
          <div className={styles.title}>당신을 위한 대출 추천</div>
          <div className={styles.subtitle}>게임에서 본 그 대출 직접 확인해볼까요?</div>
        </div>
      </div>
      <div className={styles.list}>
        {loans.map((loan, idx) => (
          <React.Fragment key={loan.productId}>
            {idx > 0 && <hr className={styles.divider} />}
            <a
              className={styles.item}
              href="https://smartmarket.nonghyup.com/servlet/BFLNW0100R.view"
              target="_blank"
              rel="noopener noreferrer"
            >
              <div
                className={styles.bankBadge}
                style={{ backgroundColor: BANK_COLORS[loan.bankName] ?? '#e5e7eb' }}
              >
                <span className={styles.bankLabel}>{loan.bankName}</span>
              </div>
              <div className={styles.itemInfo}>
                <div className={styles.itemName}>{loan.productName}</div>
                <div className={styles.itemType}>{loan.productType}</div>
              </div>
              <div className={styles.rateArea}>
                <span className={styles.rateLabel}>연이율</span>
                <span className={styles.rate}>
                  {loan.minRate.toFixed(2)} %
                </span>
                <span className={styles.rateSub}>~ {loan.maxRate.toFixed(2)}%</span>
              </div>
            </a>
          </React.Fragment>
        ))}
      </div>
    </div>
  );
};
