import type { TurnCommitResult, TurnStatChanges } from '@features/game/domain/entities/GameTurn';
import { formatMoney } from '@shared/utils/formatter';
import styles from './GameStatusPanel.module.css';

interface GameStatusPanelProps {
  turnCommitResult: TurnCommitResult | null;
}

const STAT_ITEMS: ReadonlyArray<{
  readonly key: keyof TurnStatChanges;
  readonly label: string;
}> = [
  { key: 'health', label: '체력' },
  { key: 'fatigue', label: '피로' },
  { key: 'stress', label: '스트레스' },
  { key: 'happiness', label: '행복' },
  { key: 'knowledge', label: '지식' },
];

function formatSignedValue(value: number): string {
  return value >= 0 ? `+${formatMoney(value)}` : formatMoney(value);
}

export function GameStatusPanel({ turnCommitResult }: GameStatusPanelProps) {
  if (turnCommitResult === null) {
    return (
      <section className={styles.panel} data-guide="game-status-panel">
        <div className={styles.header}>
          <p className={styles.eyebrow}>STATUS REPORT</p>
          <h2 className={styles.title}>자산/스탯 리포트</h2>
          <p className={styles.subtitle}>
            이번 달 활동을 마치면 최근 정산 기준 자산과 스탯 변화가 여기에 정리됩니다.
          </p>
        </div>
        <div className={styles.emptyCard}>
          <strong className={styles.emptyTitle}>아직 정산된 리포트가 없습니다.</strong>
          <p className={styles.emptyDescription}>
            메인 화면에서 이번 달 활동을 진행한 뒤 턴 정산을 마치면 현금, 대출, 부동산 가치와
            최근 턴 스탯 변화가 이 패널에 기록됩니다.
          </p>
        </div>
      </section>
    );
  }

  return (
    <section className={styles.panel} data-guide="game-status-panel">
      <div className={styles.header}>
        <p className={styles.eyebrow}>STATUS REPORT</p>
        <h2 className={styles.title}>자산/스탯 리포트</h2>
        <p className={styles.subtitle}>
          {turnCommitResult.turnNumber}번째 턴 정산 기준으로 자산 요약과 최근 스탯 변화를 모아봤습니다.
        </p>
      </div>

      <div className={styles.section}>
        <div className={styles.sectionHeader}>
          <h3 className={styles.sectionTitle}>자산 요약</h3>
          <span className={styles.sectionBadge}>최근 정산 기준</span>
        </div>
        <div className={styles.assetGrid}>
          <div className={styles.assetCard}>
            <span className={styles.assetLabel}>현금</span>
            <strong className={styles.assetValue}>
              {formatMoney(turnCommitResult.updatedAssets.cash)} 원
            </strong>
          </div>
          <div className={styles.assetCard}>
            <span className={styles.assetLabel}>대출</span>
            <strong className={styles.assetValue}>
              {formatMoney(turnCommitResult.updatedAssets.loan)} 원
            </strong>
          </div>
          <div className={styles.assetCard}>
            <span className={styles.assetLabel}>부동산 가치</span>
            <strong className={styles.assetValue}>
              {formatMoney(turnCommitResult.updatedAssets.realEstateValue)} 원
            </strong>
          </div>
          <div className={`${styles.assetCard} ${styles.assetCardHighlight}`}>
            <span className={styles.assetLabel}>순자산</span>
            <strong className={styles.assetValue}>
              {formatMoney(turnCommitResult.updatedAssets.netAssets)} 원
            </strong>
          </div>
        </div>
      </div>

      <div className={styles.section}>
        <div className={styles.sectionHeader}>
          <h3 className={styles.sectionTitle}>최근 턴 스탯 변화</h3>
          <span className={styles.sectionBadge}>증감 기준</span>
        </div>
        <div className={styles.statList}>
          {STAT_ITEMS.map(({ key, label }) => {
            const value = turnCommitResult.statChanges[key];
            const isPositive = value >= 0;

            return (
              <div key={key} className={styles.statRow}>
                <span className={styles.statLabel}>{label}</span>
                <span className={isPositive ? styles.statValuePositive : styles.statValueNegative}>
                  {formatSignedValue(value)}
                </span>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}
