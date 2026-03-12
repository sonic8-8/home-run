import React from 'react';
import type { SeedMoneyAccount } from '../../../domain/entities/SeedMoneyAccount';
import styles from './SeedMoneyWidget.module.css';

interface SeedMoneyWidgetProps {
  account: SeedMoneyAccount;
}

export const SeedMoneyWidget: React.FC<SeedMoneyWidgetProps> = ({ account }) => {
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
        {account.balance.toLocaleString()}원
      </div>
      <div className={styles.actions}>
        <button className={styles.actionBtn}>조회</button>
        <button className={styles.actionBtn}>송금</button>
        <button className={styles.actionBtn}>입금</button>
      </div>
    </div>
  );
};
