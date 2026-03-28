import React, { useState } from 'react';
import type { SeedMoneyAccount } from '../../../domain/entities/SeedMoneyAccount';
import type { Spending } from '../../../domain/entities/Spending';
import { formatWon } from '../../utils/money';
import styles from './SeedMoneyWidget.module.css';

interface SeedMoneyWidgetProps {
  account: SeedMoneyAccount;
  spending: Spending | null;
}

export const SeedMoneyWidget: React.FC<SeedMoneyWidgetProps> = ({ account, spending }) => {
  const [showHistory, setShowHistory] = useState(false);

  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <div className={styles.bankIcon}>🏦</div>
        <div className={styles.accountInfo}>
          <div className={styles.accountName}>시드머니 저축 계좌</div>
          <div className={styles.accountSub}>
            {account.bankName} · {account.accountNumber}
          </div>
        </div>
      </div>
      <div className={styles.balance}>
        {formatWon(account.balance)}
      </div>
      <div className={styles.actions}>
        <button className={styles.actionBtn} onClick={() => setShowHistory((v) => !v)}>
          조회
        </button>
      </div>

      {showHistory && (
        <div className={styles.history}>
          <div className={styles.historyTitle}>
            {spending ? `${spending.month} 지출 내역` : '지출 내역'}
          </div>
          {spending && spending.categories.length > 0 ? (
            <>
              {spending.categories.map((c) => (
                <div key={c.category} className={styles.historyRow}>
                  <span className={styles.historyCat}>{c.categoryName}</span>
                  <span className={styles.historyAmount}>-{formatWon(c.amount)}</span>
                </div>
              ))}
              <div className={styles.historyTotal}>
                <span>합계</span>
                <span>-{formatWon(spending.totalExpense)}</span>
              </div>
            </>
          ) : (
            <div className={styles.historyEmpty}>이번 달 지출 내역이 없습니다.</div>
          )}
        </div>
      )}
    </div>
  );
};
