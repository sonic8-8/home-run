import { useState } from 'react';
import type { PassSubscription } from '../../../domain/entities/PassSubscription';
import type { Pass } from '../../../domain/entities/Pass';
import { PassModal } from '../PassModal/PassModal';
import styles from './PassWidget.module.css';

interface PassWidgetProps {
  subscriptions: PassSubscription[];
  allPasses: Pass[];
}

export function PassWidget({ subscriptions, allPasses }: PassWidgetProps) {
  const [modalOpen, setModalOpen] = useState(false);
  const isEmpty = subscriptions.length === 0;

  return (
    <>
      <div className={styles.card} style={{ cursor: 'pointer' }} onClick={() => setModalOpen(true)}>
        <div className={styles.header}>
          <span className={styles.headerIcon}>🔄</span>
          <span className={styles.headerTitle}>소비 통제 PASS</span>
          <span className={styles.headerDot}>·</span>
          <span className={styles.headerSub}>오늘의 절약 현황</span>
        </div>

        {isEmpty ? (
          <div className={styles.emptyState}>
            <p className={styles.emptyText}>아직 구독중인 PASS가 존재하지 않아요</p>
            <button className={styles.subscribeButton} onClick={(e) => { e.stopPropagation(); setModalOpen(true); }}>
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

      <PassModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        subscriptions={subscriptions}
        allPasses={allPasses}
        onSave={(subscriptionId) => {
          console.log('저축하기', subscriptionId);
          // TODO: API 연동
        }}
        onUnsubscribe={(subscriptionId) => {
          console.log('해지하기', subscriptionId);
          // TODO: API 연동
        }}
        onSubscribe={(passId) => {
          console.log('구독하기', passId);
          // TODO: API 연동
        }}
      />
    </>
  );
}
