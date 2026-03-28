import React from 'react';
import type { Dashboard } from '../../../domain/entities/Dashboard';
import { formatKoreanCompactManWon } from '../../utils/money';
import styles from './SummaryCards.module.css';

interface SummaryCardsProps {
  dashboard: Dashboard | null;
}

export const SummaryCards: React.FC<SummaryCardsProps> = ({ dashboard }) => {
  if (!dashboard) return null;
  const { totalAssets, monthlyIncome, monthlyExpense, incomeChangeFromLastMonth, expenseChangeFromLastMonth, nextPaydayDays } = dashboard;

  return (
    <div className={styles.grid}>
      {/* 총 자산 */}
      <div className={styles.card}>
        <div className={styles.cardHeader}>
          <span className={styles.icon}>📅</span>
          <span className={styles.label}>총 자산</span>
        </div>
        <div className={styles.amount}>
          <span className={styles.value}>{formatKoreanCompactManWon(totalAssets)}</span>
        </div>
        {incomeChangeFromLastMonth != null && incomeChangeFromLastMonth !== 0 && (
          <div className={styles.sub}>
            <span className={incomeChangeFromLastMonth > 0 ? styles.subPositive : styles.subNegative}>
              {incomeChangeFromLastMonth > 0 ? '↗' : '↘'} 전월 대비 {incomeChangeFromLastMonth > 0 ? '+' : '-'}{formatKoreanCompactManWon(incomeChangeFromLastMonth)}
            </span>
          </div>
        )}
      </div>

      {/* 이번 달 수입 */}
      <div className={styles.card}>
        <div className={styles.cardHeader}>
          <span className={styles.icon}>↙</span>
          <span className={styles.label}>이번 달 수입</span>
        </div>
        <div className={styles.amount}>
          <span className={styles.value}>{formatKoreanCompactManWon(monthlyIncome)}</span>
        </div>
        <div className={styles.sub}>
          <span className={styles.subNeutral}>급여 입금 D-{nextPaydayDays}</span>
        </div>
      </div>

      {/* 이번 달 지출 */}
      <div className={styles.card}>
        <div className={styles.cardHeader}>
          <span className={styles.icon}>↗</span>
          <span className={styles.label}>이번 달 지출</span>
        </div>
        <div className={styles.amount}>
          <span className={styles.value}>{formatKoreanCompactManWon(monthlyExpense)}</span>
        </div>
        {expenseChangeFromLastMonth != null && expenseChangeFromLastMonth !== 0 && (
          <div className={styles.sub}>
            <span className={expenseChangeFromLastMonth > 0 ? styles.subNegative : styles.subPositive}>
              {expenseChangeFromLastMonth > 0 ? '↗' : '↘'} 전월 대비 {expenseChangeFromLastMonth > 0 ? '+' : '-'}{formatKoreanCompactManWon(expenseChangeFromLastMonth)}
            </span>
          </div>
        )}
      </div>
    </div>
  );
};
