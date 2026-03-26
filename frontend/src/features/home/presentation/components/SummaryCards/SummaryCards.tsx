import React from 'react';
import type { Dashboard } from '../../../domain/entities/Dashboard';
import styles from './SummaryCards.module.css';

interface SummaryCardsProps {
  dashboard: Dashboard | null;
}

const toMan = (value: number) => Math.round(Math.abs(value) / 10000).toLocaleString();

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
          <span className={styles.value}>{toMan(totalAssets)}</span>
          <span className={styles.unit}>만원</span>
        </div>
        {incomeChangeFromLastMonth != null && incomeChangeFromLastMonth !== 0 && (
          <div className={styles.sub}>
            <span className={incomeChangeFromLastMonth > 0 ? styles.subPositive : styles.subNegative}>
              {incomeChangeFromLastMonth > 0 ? '↗' : '↘'} 전월 대비 {incomeChangeFromLastMonth > 0 ? '+' : '-'}{toMan(incomeChangeFromLastMonth)}만원
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
          <span className={styles.value}>{toMan(monthlyIncome)}</span>
          <span className={styles.unit}>만원</span>
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
          <span className={styles.value}>{toMan(monthlyExpense)}</span>
          <span className={styles.unit}>만원</span>
        </div>
        {expenseChangeFromLastMonth != null && expenseChangeFromLastMonth !== 0 && (
          <div className={styles.sub}>
            <span className={expenseChangeFromLastMonth > 0 ? styles.subNegative : styles.subPositive}>
              {expenseChangeFromLastMonth > 0 ? '↗' : '↘'} 전월 대비 {expenseChangeFromLastMonth > 0 ? '+' : '-'}{toMan(expenseChangeFromLastMonth)}만원
            </span>
          </div>
        )}
      </div>
    </div>
  );
};
