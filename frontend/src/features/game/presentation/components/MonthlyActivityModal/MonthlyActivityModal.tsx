import React, { useState } from 'react';
import clsx from 'clsx';
import type { TurnAction } from '@features/game/domain/entities/TurnAction';
import styles from './MonthlyActivityModal.module.css';

const MAX_SLOTS = 3;

const EFFECT_LABEL: Record<string, string> = {
  cash: '자금',
  health: '체력',
  knowledge: '지능',
  happiness: '행복도',
  fatigue: '피로도',
  stress: '스트레스',
};

const ITEMS_PER_PAGE = 3;

function formatEffect(value: number): string {
  return value >= 0 ? `+${value.toLocaleString('ko-KR')}` : value.toLocaleString('ko-KR');
}

interface MonthlyActivityModalProps {
  isOpen: boolean;
  month: number;
  shopping: TurnAction[];
  activities: TurnAction[];
  onStart: (slots: string[]) => void;
}

interface ActionCardProps {
  action: TurnAction;
  count: number;
  totalSelected: number;
  onAdd: () => void;
  onRemove: () => void;
}

const ActionCard: React.FC<ActionCardProps> = ({ action, count, totalSelected, onAdd, onRemove }) => {
  const isSelected = count > 0;
  const canAdd = totalSelected < MAX_SLOTS;

  return (
    <div
      className={clsx(styles.card, isSelected && styles.cardSelected)}
      onClick={() => canAdd && onAdd()}
    >
      {isSelected && (
        <button
          className={styles.removeBtn}
          onClick={(e) => { e.stopPropagation(); onRemove(); }}
        >
          ✕
        </button>
      )}
      {count > 1 && (
        <span className={styles.countBadge}>{count}</span>
      )}
      <img className={styles.cardIcon} src={action.iconUrl} alt={action.label} />
      <div className={styles.effectList}>
        {Object.entries(action.effects).map(([key, value]) => (
          <div key={key} className={styles.effectRow}>
            <span className={styles.effectLabel}>{EFFECT_LABEL[key] ?? key}</span>
            <span className={clsx(styles.effectValue, (value ?? 0) < 0 ? styles.negative : styles.positive)}>
              {formatEffect(value ?? 0)}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
};

export const MonthlyActivityModal: React.FC<MonthlyActivityModalProps> = ({
  isOpen,
  month,
  shopping,
  activities,
  onStart,
}) => {
  const [shopPage, setShopPage] = useState(0);
  const [actPage, setActPage] = useState(0);
  // Record<actionType, count>
  const [counts, setCounts] = useState<Record<string, number>>({});

  if (!isOpen) return null;

  const totalSelected = Object.values(counts).reduce((sum, n) => sum + n, 0);

  const addAction = (actionType: string) => {
    if (totalSelected >= MAX_SLOTS) return;
    setCounts((prev) => ({ ...prev, [actionType]: (prev[actionType] ?? 0) + 1 }));
  };

  const removeAction = (actionType: string) => {
    setCounts((prev) => {
      const next = { ...prev };
      if ((next[actionType] ?? 0) > 1) {
        next[actionType]--;
      } else {
        delete next[actionType];
      }
      return next;
    });
  };

  const handleStart = () => {
    const slots: string[] = [];
    Object.entries(counts).forEach(([actionType, count]) => {
      for (let i = 0; i < count; i++) slots.push(actionType);
    });
    onStart(slots);
  };

  const shopSlice = shopping.slice(shopPage * ITEMS_PER_PAGE, shopPage * ITEMS_PER_PAGE + ITEMS_PER_PAGE);
  const actSlice = activities.slice(actPage * ITEMS_PER_PAGE, actPage * ITEMS_PER_PAGE + ITEMS_PER_PAGE);
  const maxShopPage = Math.max(0, Math.ceil(shopping.length / ITEMS_PER_PAGE) - 1);
  const maxActPage = Math.max(0, Math.ceil(activities.length / ITEMS_PER_PAGE) - 1);

  return (
    <div className={styles.overlay}>
      <div className={styles.modal}>
        {/* 제목 */}
        <h2 className={styles.title}>{month}월 활동</h2>

        {/* 슬롯 카운터 */}
        <div className={styles.slotCounter}>
          <span className={styles.slotCount}>{totalSelected}</span>
          <span className={styles.slotMax}>/ {MAX_SLOTS} 선택</span>
        </div>

        {/* 쇼핑 카테고리 */}
        <section className={styles.category}>
          <span className={styles.categoryLabel}>쇼핑</span>
          <div className={styles.carousel}>
            <button
              className={styles.arrowBtn}
              onClick={() => setShopPage((p) => Math.max(0, p - 1))}
              disabled={shopPage === 0}
            >
              ◀
            </button>
            <div className={styles.cardRow}>
              {shopSlice.map((action) => (
                <ActionCard
                  key={action.actionType}
                  action={action}
                  count={counts[action.actionType] ?? 0}
                  totalSelected={totalSelected}
                  onAdd={() => addAction(action.actionType)}
                  onRemove={() => removeAction(action.actionType)}
                />
              ))}
            </div>
            <button
              className={styles.arrowBtn}
              onClick={() => setShopPage((p) => Math.min(maxShopPage, p + 1))}
              disabled={shopPage >= maxShopPage}
            >
              ▶
            </button>
          </div>
        </section>

        {/* 활동 카테고리 */}
        <section className={styles.category}>
          <span className={styles.categoryLabel}>활동</span>
          <div className={styles.carousel}>
            <button
              className={styles.arrowBtn}
              onClick={() => setActPage((p) => Math.max(0, p - 1))}
              disabled={actPage === 0}
            >
              ◀
            </button>
            <div className={styles.cardRow}>
              {actSlice.map((action) => (
                <ActionCard
                  key={action.actionType}
                  action={action}
                  count={counts[action.actionType] ?? 0}
                  totalSelected={totalSelected}
                  onAdd={() => addAction(action.actionType)}
                  onRemove={() => removeAction(action.actionType)}
                />
              ))}
            </div>
            <button
              className={styles.arrowBtn}
              onClick={() => setActPage((p) => Math.min(maxActPage, p + 1))}
              disabled={actPage >= maxActPage}
            >
              ▶
            </button>
          </div>
        </section>

        {/* START 버튼 */}
        <button
          className={styles.startButton}
          onClick={handleStart}
          disabled={totalSelected !== MAX_SLOTS}
        >
          START
        </button>
      </div>
    </div>
  );
};
