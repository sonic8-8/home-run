import type { GameSlot } from '@features/game/domain/entities/GameSlot';
import { JOB_TYPE_LABELS } from '@features/game/domain/constants/jobTypeLabels';
import { useGameSaveSlots } from '../../hooks/useGameSaveSlots';
import styles from './GameSaveSlot.module.css';

const formatAssets = (value: number): string =>
  `${value.toLocaleString('ko-KR')} 원`;

const formatDate = (dateStr: string): string => {
  const [year, month, day] = dateStr.split('-');
  return `${year.slice(2)}년 ${month}월 ${day}일`;
};

interface SlotCardProps {
  slot: GameSlot;
  onSelect: (slot: GameSlot) => void;
}

const SlotCard = ({ slot, onSelect }: SlotCardProps) => {
  const isEmpty = slot.status === 'EMPTY';

  return (
    <button
      className={`${styles.slot} ${isEmpty ? styles.slotEmpty : styles.slotActive}`}
      onClick={() => onSelect(slot)}
      disabled={isEmpty}
      aria-label={isEmpty ? `슬롯 ${slot.slotNumber} 비어 있음` : `슬롯 ${slot.slotNumber} 불러오기`}
    >
      <div className={styles.slotNumber}>
        <span>SLOT</span>
        <span>{slot.slotNumber}</span>
      </div>
      <div className={styles.slotInfo}>
        {isEmpty ? (
          <span className={styles.emptyLabel}>빈 슬롯 입니다.</span>
        ) : (
          <>
            <div className={styles.slotRow}>
              <span className={styles.characterName}>{slot.characterName}</span>
              <span className={styles.jobTitle}>
                {slot.jobType ? JOB_TYPE_LABELS[slot.jobType] : ''}
              </span>
            </div>
            <div className={styles.slotRow}>
              <span className={styles.date}>
                {slot.createdAt ? formatDate(slot.createdAt) : ''}
              </span>
              <span className={styles.assets}>
                총자산: {slot.totalAssets !== undefined ? formatAssets(slot.totalAssets) : ''}
              </span>
            </div>
          </>
        )}
      </div>
    </button>
  );
};

export default function GameSaveSlot() {
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
