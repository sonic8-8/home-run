import { useState } from 'react';
import type { PassSubscription } from '../../../domain/entities/PassSubscription';
import type { Pass } from '../../../domain/entities/Pass';
import { PassModal } from '../PassModal/PassModal';
import { formatWon } from '../../utils/money';
import styles from './PassWidget.module.css';

interface PassWidgetProps {
  subscriptions: PassSubscription[];
  allPasses: Pass[];
  onSave: (subscriptionId: number) => void;
  onUnsubscribe: (subscriptionId: number) => void;
  onSubscribe: (passId: number) => void;
}

export function PassWidget({ subscriptions, allPasses, onSave, onUnsubscribe, onSubscribe }: PassWidgetProps) {
  const [modalOpen, setModalOpen] = useState(false);
  const isEmpty = subscriptions.length === 0;
  const totalSaved = subscriptions.reduce((sum, s) => sum + s.totalSaved, 0);

  return (
    <>
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
            <button className={styles.subscribeButton} onClick={() => setModalOpen(true)}>
              구독하러 가기
            </button>
          </div>
        ) : (
          <>
            <div className={styles.totalRow}>
              <span className={styles.totalLabel}>총 저축 금액</span>
              <span className={styles.totalAmount}>{formatWon(totalSaved)}</span>
            </div>
            <button className={styles.saveButton} onClick={() => setModalOpen(true)}>
              저축하기
            </button>
          </>
        )}
      </div>

      <PassModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        subscriptions={subscriptions}
        allPasses={allPasses}
        onSave={onSave}
        onUnsubscribe={onUnsubscribe}
        onSubscribe={onSubscribe}
      />
    </>
  );
}
