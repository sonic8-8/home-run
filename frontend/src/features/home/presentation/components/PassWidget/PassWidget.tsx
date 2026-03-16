import React from 'react';
import { useNavigate } from 'react-router-dom';
import type { PassSubscription } from '../../../domain/entities/PassSubscription';
import styles from './PassWidget.module.css';

interface PassWidgetProps {
  subscriptions: PassSubscription[];
}

export const PassWidget: React.FC<PassWidgetProps> = ({ subscriptions }) => {
  const navigate = useNavigate();
  const isEmpty = subscriptions.length === 0;

  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <span className={styles.headerIcon}>🔄</span>
        <span className={styles.headerTitle}>소비 통제 PASS</span>
        <span className={styles.headerDot}>·</span>
        <span className={styles.headerSub}>오늘의 절약 현황</span>
      </div>

      {isEmpty ? (
        <div className={styles.emptyState}>
          <p className={styles.emptyText}>아직 구독중인 PASS가 존재하지 않아요</p>
          <button
            className={styles.subscribeButton}
            onClick={() => navigate('/pass')}
          >
            구독하러 가기
          </button>
        </div>
      ) : (
        <div className={styles.list}>
          {subscriptions.map((sub) => (
            <div key={sub.subscriptionId} className={styles.item}>
              <span className={styles.itemName}>{sub.name}</span>
              <span className={styles.itemSaved}>{sub.totalSaved.toLocaleString()}원 저축</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
