import clsx from 'clsx';
import type { GameSlot } from '@features/game/domain/entities/GameSlot';
import { JOB_TYPE_LABELS } from '@features/game/domain/constants/jobTypeLabels';
import { useGameSaveSlots } from '../../hooks/useGameSaveSlots';
import styles from './GameSaveSlotPage.module.css';

const formatAssets = (value: number): string =>
  `${value.toLocaleString('ko-KR')} 원`;

const SESSION_STATUS_LABELS: Record<
  Exclude<GameSlot['status'], 'EMPTY'>,
  string
> = {
  IN_PROGRESS: '진행 중',
  CLEAR: '클리어',
  BANKRUPT: '파산',
  TIMEOUT: '시간 초과',
  FORECLOSURE: '압류',
};

const formatDate = (dateStr?: string): string => {
  if (!dateStr) {
    return '';
  }
  const [dateOnly] = dateStr.split('T');
  const [year, month, day] = dateOnly.split('-');
  if (!year || !month || !day) {
    return dateStr;
  }
  return `${year.slice(2)}년 ${month}월 ${day}일`;
};

const getSlotMeta = (slot: GameSlot): string => {
  const parts: string[] = [];

  if (slot.jobType) {
    parts.push(JOB_TYPE_LABELS[slot.jobType]);
  }

  if (slot.status === 'IN_PROGRESS' && slot.currentTurn !== undefined) {
    parts.push(`${slot.currentTurn}턴`);
  }

  if (slot.status !== 'EMPTY' && slot.status !== 'IN_PROGRESS') {
    parts.push(SESSION_STATUS_LABELS[slot.status]);
  }

  return parts.join(' · ');
};

interface SlotCardProps {
  slot: GameSlot;
  onSelect: (slot: GameSlot) => void;
}

const SlotCard = ({ slot, onSelect }: SlotCardProps) => {
  const isEmpty = slot.status === 'EMPTY';
  const isEnded = slot.status !== 'EMPTY' && slot.status !== 'IN_PROGRESS';

  return (
    <button
      className={clsx(
        styles.slot,
        isEmpty && styles.slotEmpty,
        isEnded && styles.slotEnded,
      )}
      onClick={() => onSelect(slot)}
      aria-label={
        isEmpty
          ? `슬롯 ${slot.slotNumber} 새 게임 시작`
          : `슬롯 ${slot.slotNumber} 불러오기`
      }
    >
      <div className={styles.slotNumber}>
        <span>SLOT</span>
        <span>{slot.slotNumber}</span>
      </div>
      <div className={styles.slotInfo}>
        {isEmpty ? (
          <span className={styles.emptyLabel}>빈 슬롯 - 새 게임 시작</span>
        ) : (
          <>
            <div className={styles.slotRow}>
              <span className={styles.characterName}>{slot.characterName}</span>
              <span className={styles.jobTitle}>{getSlotMeta(slot)}</span>
            </div>
            <div className={styles.slotRow}>
              <span className={styles.date}>{formatDate(slot.createdAt)}</span>
              <span className={styles.assets}>
                총자산{' '}
                {slot.totalAssets !== undefined
                  ? formatAssets(slot.totalAssets)
                  : '-'}
              </span>
            </div>
          </>
        )}
      </div>
    </button>
  );
};

export function GameSaveSlotPage() {
  const { slots, isLoading, error, handleSelectSlot } = useGameSaveSlots();

  return (
    <div className={styles.page}>
      <div className={styles.container}>
        <div className={styles.title}>SAVE</div>
        {isLoading && <p className={styles.message}>불러오는 중...</p>}
        {error && <p className={styles.message}>{error}</p>}
        {!isLoading && !error && slots.map((slot) => (
          <SlotCard key={slot.slotNumber} slot={slot} onSelect={handleSelectSlot} />
        ))}
      </div>
    </div>
  );
}
