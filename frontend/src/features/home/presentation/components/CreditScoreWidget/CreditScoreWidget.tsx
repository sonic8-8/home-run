import React from 'react';
import type { CreditScore } from '../../../domain/entities/CreditScore';
import styles from './CreditScoreWidget.module.css';

interface CreditScoreWidgetProps {
  creditScore: CreditScore;
}

export const CreditScoreWidget: React.FC<CreditScoreWidgetProps> = ({ creditScore }) => {
  const { kcbScore, niceScore, baseScore, estimatedMinRate } = creditScore;

  return (
    <div className={styles.card}>
      <div className={styles.title}>신용등급</div>
      <div className={styles.scoreList}>
        <div className={styles.scoreRow}>
          <span className={`${styles.scoreName} ${styles.kcb}`}>KCB</span>
          <span className={`${styles.scoreValue} ${styles.kcb}`}>{kcbScore}</span>
        </div>
        <div className={styles.scoreRow}>
          <span className={`${styles.scoreName} ${styles.nice}`}>NICE</span>
          <span className={`${styles.scoreValue} ${styles.nice}`}>{niceScore}</span>
        </div>
      </div>
      <hr className={styles.divider} />
      <div className={styles.rateSection}>
        <div className={styles.rateLabel}>신용등급 {baseScore}점일 때</div>
        <div className={styles.rateRow}>
          <span className={styles.rateDesc}>예상 최저 금리</span>
          <span className={styles.rateValue}>{estimatedMinRate.toFixed(2)}%</span>
        </div>
      </div>
      <button className={styles.detailBtn}>
        신용등급 조회하기 &gt;
      </button>
    </div>
  );
};
