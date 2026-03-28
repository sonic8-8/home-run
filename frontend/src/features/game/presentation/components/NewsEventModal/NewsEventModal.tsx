import clsx from 'clsx';
import type { NewsItem, TurnNews } from '@features/game/domain/entities/GameTurn';
import { getEconomicCycleLabel, isNegativeEconomicCycle } from '@features/game/utils/economicCycle';
import { formatIsoDate } from '@shared/utils/formatter';
import styles from './NewsEventModal.module.css';

interface Props {
  isOpen: boolean;
  onClose: () => void;
  news: TurnNews | null;
  loading: boolean;
  error: string | null;
}

function NewsCard({ item }: { item: NewsItem }) {
  const cycleNeg = isNegativeEconomicCycle(item.economicCycleType);

  return (
    <article className={styles.article}>
      <div className={styles.articleMeta}>
        <span className={clsx(styles.cycleBadge, cycleNeg ? styles.cycleBadgeNeg : styles.cycleBadgePos)}>
          {getEconomicCycleLabel(item.economicCycleType)}
        </span>
        <span className={styles.source}>{item.sourceName}</span>
        <span className={styles.date}>{formatIsoDate(item.publishedDate)}</span>
      </div>
      <h2 className={styles.headline}>{item.headline}</h2>
      <p className={styles.content}>{item.content}</p>
    </article>
  );
}

export function NewsEventModal({ isOpen, onClose, news, loading, error }: Props) {
  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        {/* 신문 헤더 */}
        <div className={styles.masthead}>
          <div className={styles.mastheadLine} />
          <div className={styles.mastheadTitle}>홈런 경제 신문</div>
          <div className={styles.mastheadSub}>
            {news ? `${formatIsoDate(news.currentDate)} · ${news.turnNumber}번째 달` : '이달의 경제 뉴스'}
          </div>
          <div className={styles.mastheadLine} />
        </div>

        {/* 뉴스 목록 */}
        <div className={styles.body}>
          {loading && <div className={styles.loading}>뉴스를 불러오는 중...</div>}
          {!loading && error && <div className={styles.empty}>{error}</div>}
          {!loading && news && news.news.length === 0 && (
            <div className={styles.empty}>이달 뉴스가 없습니다.</div>
          )}
          {!loading && news && news.news.map((item) => (
            <NewsCard key={item.newsId} item={item} />
          ))}
        </div>

        {/* 푸터 */}
        <div className={styles.footer}>
          <button className={styles.confirmBtn} onClick={onClose}>확인</button>
        </div>
      </div>
    </div>
  );
}
