import clsx from 'clsx';
import { useState } from 'react';
import type { NewsItem } from '@features/game/domain/entities/GameTurn';
import { getEconomicCycleLabel, isNegativeEconomicCycle } from '@features/game/utils/economicCycle';
import { formatIsoDate } from '@shared/utils/formatter';
import styles from './NewsArticle.module.css';

interface NewsArticleProps {
  item: NewsItem;
}

export function NewsArticle({ item }: NewsArticleProps) {
  const [expanded, setExpanded] = useState(false);
  const cycleNeg = isNegativeEconomicCycle(item.economicCycleType);

  return (
    <div className={styles.card} onClick={() => setExpanded((value) => !value)}>
      <div className={styles.cardMeta}>
        <span
          className={clsx(
            styles.cycleBadge,
            cycleNeg ? styles.cycleNeg : styles.cyclePos,
          )}
        >
          {getEconomicCycleLabel(item.economicCycleType)}
        </span>
        <span className={styles.source}>{item.sourceName}</span>
        <span className={styles.date}>{formatIsoDate(item.publishedDate)}</span>
      </div>
      <h3 className={styles.headline}>{item.headline}</h3>
      {expanded && <p className={styles.content}>{item.content}</p>}
      <button className={styles.toggleBtn} type="button">
        {expanded ? '접기 ▲' : '본문 보기 ▼'}
      </button>
    </div>
  );
}
