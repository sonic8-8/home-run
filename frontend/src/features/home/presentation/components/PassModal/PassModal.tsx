import { useState } from 'react';
import type { PassSubscription } from '../../../domain/entities/PassSubscription';
import type { Pass } from '../../../domain/entities/Pass';
import styles from './PassModal.module.css';

const DAY_LABELS = ['월', '화', '수', '목', '금', '토', '일'];

interface PassModalProps {
  isOpen: boolean;
  onClose: () => void;
  subscriptions: PassSubscription[];
  allPasses: Pass[];
  onSave: (subscriptionId: number) => void;
  onUnsubscribe: (subscriptionId: number) => void;
  onSubscribe: (passId: number) => void;
}

type Tab = 'my' | 'all';

export function PassModal({
  isOpen,
  onClose,
  subscriptions,
  allPasses,
  onSave,
  onUnsubscribe,
  onSubscribe,
}: PassModalProps) {
  const [activeTab, setActiveTab] = useState<Tab>('my');

  if (!isOpen) return null;

  const subscribedPassIds = new Set(subscriptions.map((s) => s.passId));

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        {/* 탭 */}
        <div className={styles.tabs}>
          <button
            className={activeTab === 'my' ? `${styles.tab} ${styles.tabActive}` : styles.tab}
            onClick={() => setActiveTab('my')}
          >
            내 패스
          </button>
          <button
            className={activeTab === 'all' ? `${styles.tab} ${styles.tabActive}` : styles.tab}
            onClick={() => setActiveTab('all')}
          >
            전체 패스
          </button>
        </div>

        {/* 내 패스 */}
        {activeTab === 'my' && (
          <div className={styles.content}>
            {subscriptions.length === 0 ? (
              <p className={styles.emptyState}>구독 중인 PASS가 없습니다.</p>
            ) : (
              subscriptions.map((sub) => (
                <div key={sub.subscriptionId} className={styles.passCard}>
                  <div className={styles.passHeader}>
                    <div className={styles.passInfo}>
                      <span className={styles.passName}>{sub.name}</span>
                      <span className={styles.passDesc}>
                        저축하기 버튼 클릭시 {sub.amountPerSave.toLocaleString()}원씩 저축
                      </span>
                    </div>
                    <div className={styles.passTotalBox}>
                      <span className={styles.passTotalLabel}>총금액</span>
                      <span className={styles.passTotalAmount}>
                        {sub.totalSaved.toLocaleString()} 원
                      </span>
                    </div>
                  </div>

                  <div className={styles.weekSection}>
                    <span className={styles.weekLabel}>최근 일주일 내역</span>
                    <div className={styles.weekDots}>
                      {DAY_LABELS.map((label, i) => (
                        <div
                          key={label}
                          className={
                            sub.weeklyHistory[i]
                              ? styles.dayDot
                              : `${styles.dayDot} ${styles.dayDotEmpty}`
                          }
                        >
                          {label}
                        </div>
                      ))}
                    </div>
                  </div>

                  <div className={styles.passButtons}>
                    <button className={styles.saveButton} onClick={() => onSave(sub.subscriptionId)}>
                      저축하기
                    </button>
                    <button className={styles.cancelButton} onClick={() => onUnsubscribe(sub.subscriptionId)}>
                      해지하기
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {/* 전체 패스 */}
        {activeTab === 'all' && (
          <div className={styles.content}>
            <div className={styles.grid}>
              {allPasses.map((pass) => {
                const isSubscribed = subscribedPassIds.has(pass.passId);
                return (
                  <div key={pass.passId} className={styles.gridCard}>
                    <span className={styles.gridName}>{pass.name}</span>
                    <span className={styles.gridAmount}>
                      {pass.amountPerSave.toLocaleString()}원 / 회
                    </span>
                    <span className={styles.gridDesc}>{pass.description}</span>
                    {isSubscribed ? (
                      <button className={styles.subscribedButton} disabled>
                        구독중
                      </button>
                    ) : (
                      <button
                        className={styles.subscribeButton}
                        onClick={() => onSubscribe(pass.passId)}
                      >
                        구독하기
                      </button>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
