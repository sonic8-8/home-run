import { useState } from 'react';
import type { GameAssets } from '@features/game/domain/entities/GameAssets';
import styles from './AssetsDetailModal.module.css';

interface Props {
  isOpen: boolean;
  onClose: () => void;
  assets: GameAssets | null;
}

type Tab = 'assets' | 'stocks' | 'news';

const HOUSING_LABEL: Record<string, string> = {
  NONE:       '',
  STUDIO:     '원룸',
  VILLA:      '빌라',
  JEONSE_APT: '전세',
  OWNED_APT:  '자가',
};

function formatMoney(amount: number): string {
  return amount.toLocaleString('ko-KR');
}

function formatSalary(amount: number): string {
  return `${(amount / 10_000).toLocaleString('ko-KR')} 만원`;
}

export function AssetsDetailModal({ isOpen, onClose, assets }: Props) {
  const [activeTab, setActiveTab] = useState<Tab>('assets');

  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        {/* 탭 헤더 */}
        <div className={styles.header}>
          <div className={styles.tabs}>
            <button
              className={activeTab === 'assets' ? styles.tabActive : styles.tab}
              onClick={() => setActiveTab('assets')}
            >
              내자산
            </button>
            <button
              className={activeTab === 'stocks' ? styles.tabActive : styles.tab}
              onClick={() => setActiveTab('stocks')}
            >
              주식
            </button>
            <button
              className={activeTab === 'news' ? styles.tabActive : styles.tab}
              onClick={() => setActiveTab('news')}
            >
              지난뉴스
            </button>
          </div>
          <button className={styles.saveButton} title="저장">
            💾
          </button>
        </div>

        {/* 내자산 탭 */}
        {activeTab === 'assets' && assets && (
          <div className={styles.content}>
            {/* 현금 / 부동산 */}
            <div className={styles.row2}>
              <div className={styles.card}>
                <span className={styles.cardLabel}>현금자산</span>
                <span className={styles.cardValueLg}>
                  {formatMoney(assets.cash)} 원
                </span>
              </div>

              <div className={styles.card}>
                <span className={styles.cardLabel}>부동산</span>
                {assets.realEstate ? (
                  <>
                    <span className={styles.cardValueMd}>
                      {assets.realEstate.propertyName}
                    </span>
                    {HOUSING_LABEL[assets.realEstate.housingType] && (
                      <span className={styles.housingBadge}>
                        {HOUSING_LABEL[assets.realEstate.housingType]}
                      </span>
                    )}
                  </>
                ) : (
                  <span className={styles.cardEmpty}>없음</span>
                )}
              </div>
            </div>

            {/* 대출 / 주식 */}
            <div className={styles.row2}>
              <div className={styles.card}>
                <span className={styles.cardLabel}>대출</span>
                {assets.loan ? (
                  <div className={styles.loanDetail}>
                    <div className={styles.loanRow}>
                      <span className={styles.loanKey}>원금</span>
                      <span className={styles.loanVal}>
                        {formatMoney(assets.loan.principal)} 원
                      </span>
                    </div>
                    <div className={styles.loanRow}>
                      <span className={styles.loanKey}>이자</span>
                      <span className={styles.loanVal}>
                        {formatMoney(assets.loan.monthlyInterest)} 원
                      </span>
                    </div>
                  </div>
                ) : (
                  <span className={styles.cardEmpty}>없음</span>
                )}
              </div>

              <div className={`${styles.card} ${assets.stock ? styles.cardHighlight : ''}`}>
                <div className={styles.stockHeader}>
                  <span className={styles.cardLabel}>주식</span>
                  <button className={styles.stockDetail}>자세히</button>
                </div>
                {assets.stock && assets.stock.holdings.length > 0 ? (
                  assets.stock.holdings.map((h) => (
                    <div key={h.stockCode} className={styles.stockRow}>
                      <span className={styles.stockName}>{h.stockName}</span>
                      <div className={styles.stockRight}>
                        <span className={styles.stockValue}>
                          {formatMoney(h.currentValue)} 원
                        </span>
                        <span className={styles.stockQty}>{h.quantity}주</span>
                      </div>
                    </div>
                  ))
                ) : (
                  <span className={styles.cardEmpty}>없음</span>
                )}
              </div>
            </div>

            {/* 커리어 */}
            <div className={styles.career}>
              <span className={styles.careerName}>{assets.career.characterName}</span>
              <span className={styles.careerJob}>{assets.career.jobTitle}</span>
              <span className={styles.careerSalary}>
                연봉: {formatSalary(assets.career.annualSalary)}
              </span>
            </div>

            {/* 가능 부업 */}
            {assets.sideJobs.length > 0 && (
              <div className={styles.sideJobSection}>
                <span className={styles.sideJobTitle}>가능 부업</span>
                <div className={styles.sideJobList}>
                  {assets.sideJobs.map((job) => (
                    <div key={job.sideJobId} className={styles.sideJobCard}>
                      <span className={styles.sideJobName}>{job.name}</span>
                      <span className={styles.sideJobEffect}>
                        돈: + {formatMoney(job.cashEffect)} 원
                      </span>
                      <span className={styles.sideJobEffect}>
                        체력: {job.healthEffect}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}

        {/* 주식 탭 */}
        {activeTab === 'stocks' && (
          <div className={styles.content}>
            <p className={styles.placeholder}>주식 상세 — 준비 중</p>
          </div>
        )}

        {/* 지난뉴스 탭 */}
        {activeTab === 'news' && (
          <div className={styles.content}>
            <p className={styles.placeholder}>지난 뉴스 — 준비 중</p>
          </div>
        )}
      </div>
    </div>
  );
}

export default AssetsDetailModal;
