import { useState } from 'react';
import type { PassSubscription } from '../../../domain/entities/PassSubscription';
import type { Pass } from '../../../domain/entities/Pass';
import type { PassHistoryPage } from '../../../domain/entities/PassHistory';
import { PassModal } from '../PassModal/PassModal';
import { formatWon } from '../../utils/money';
import styles from './PassWidget.module.css';

interface PassWidgetProps {
  subscriptions: PassSubscription[];
  history: PassHistoryPage | null;
  allPasses: Pass[];
  onSave: (subscriptionId: number) => void;
  onUnsubscribe: (subscriptionId: number) => void;
  onSubscribe: (passId: number) => void;
}

function formatSavedAt(value: string): string {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat('ko-KR', {
    month: 'short',
    day: 'numeric',
  }).format(date);
}

export function PassWidget({
  subscriptions,
  history,
  allPasses,
  onSave,
  onUnsubscribe,
  onSubscribe,
}: PassWidgetProps) {
  const [modalOpen, setModalOpen] = useState(false);
  const isEmpty = subscriptions.length === 0;
  const totalSaved = subscriptions.reduce((sum, s) => sum + s.totalSaved, 0);
  const recentHistory = history?.content ?? [];

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

        {recentHistory.length > 0 && (
          <div className={styles.historySection} data-testid="pass-history-list">
            <div className={styles.historyHeader}>
              <span className={styles.historyTitle}>최근 PASS 저축 기록</span>
              <span className={styles.historyCount}>총 {history?.totalElements ?? recentHistory.length}건</span>
            </div>
            <div className={styles.historyList}>
              {recentHistory.map((item) => (
                <div key={item.historyId} className={styles.historyItem}>
                  <div className={styles.historyMain}>
                    <span className={styles.historyName}>{item.passName}</span>
                    <span className={styles.historyDate}>{formatSavedAt(item.savedAt)}</span>
                  </div>
                  <span className={styles.historyAmount}>+{formatWon(item.amount)}</span>
                </div>
              ))}
            </div>
          </div>
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
