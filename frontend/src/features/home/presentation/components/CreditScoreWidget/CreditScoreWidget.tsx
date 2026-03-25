import React from 'react';
import type { CreditScore } from '../../../domain/entities/CreditScore';
import styles from './CreditScoreWidget.module.css';

interface CreditScoreWidgetProps {
  creditScore: CreditScore | null;
}

const GRADE_COLORS: Record<number, string> = {
  1: '#7c3aed',
  2: '#2563eb',
  3: '#16a34a',
  4: '#d97706',
  5: '#dc2626',
};

const FICO_ITEMS = [
  { key: 'paymentHistory', label: '납부 이력', max: 350 },
  { key: 'amountsOwed',    label: '부채 수준', max: 300 },
  { key: 'creditLength',   label: '신용 기간', max: 150 },
  { key: 'creditMix',      label: '신용 다양성', max: 100 },
  { key: 'newCredit',      label: '신규 신용', max: 100 },
] as const;

export const CreditScoreWidget: React.FC<CreditScoreWidgetProps> = ({ creditScore }) => {
  if (!creditScore) {
    return (
      <div className={styles.card}>
        <div className={styles.title}>신용점수</div>
        <div className={styles.empty}>신용점수를 불러오는 중...</div>
      </div>
    );
  }

  const { score, grade, gradeLabel } = creditScore;
  const color = GRADE_COLORS[grade] ?? '#6b7280';

  return (
    <div className={styles.card}>
      <div className={styles.title}>신용점수</div>

      <div className={styles.scoreArea}>
        <div className={styles.scoreMain} style={{ color }}>
          {score}
          <span className={styles.scoreMax}> / 1000</span>
        </div>
        <div className={styles.gradeBadge} style={{ backgroundColor: color }}>
          {grade}등급 · {gradeLabel}
        </div>
      </div>

      <hr className={styles.divider} />

      <div className={styles.ficoList}>
        {FICO_ITEMS.map(({ key, label, max }) => {
          const val = creditScore[key];
          const pct = Math.round((val / max) * 100);
          return (
            <div key={key} className={styles.ficoRow}>
              <div className={styles.ficoMeta}>
                <span className={styles.ficoLabel}>{label}</span>
                <span className={styles.ficoValue}>{val} <span className={styles.ficoMax}>/ {max}</span></span>
              </div>
              <div className={styles.ficoBarBg}>
                <div className={styles.ficoBar} style={{ width: `${pct}%`, backgroundColor: color }} />
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
