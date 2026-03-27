import { useEffect } from 'react';
import { useGameWorld } from '@features/game/presentation/hooks/useGameWorld';
import type { NewsItem } from '@features/game/domain/entities/GameTurn';
import styles from './NewsEventModal.module.css';

interface Props {
  isOpen: boolean;
  onClose: () => void;
  sessionId: number;
}

const CYCLE_LABELS: Record<string, string> = {
  BOOM_TO_BOOM:           '호황 지속',
  BOOM_TO_CRISIS:         '호황 → 위기',
  BOOM_TO_RECOVERY:       '호황 → 회복',
  CRISIS_TO_CRISIS:       '위기 지속',
  CRISIS_TO_RECOVERY:     '위기 → 회복',
  CRISIS_TO_BOOM:         '위기 → 호황',
  RECOVERY_TO_BOOM:       '회복 → 호황',
  RECOVERY_TO_RECOVERY:   '회복 지속',
  RECOVERY_TO_CRISIS:     '회복 → 위기',
  EXPANSION:              '경기 확장',
  CONTRACTION:            '경기 수축',
  RECOVERY:               '경기 회복',
  PEAK:                   '경기 정점',
  TROUGH:                 '경기 저점',
};

const SENTIMENT_LABELS: Record<string, { label: string; cls: string }> = {
  positive: { label: '긍정', cls: styles.sentimentPos },
  negative: { label: '부정', cls: styles.sentimentNeg },
  mixed:    { label: '복합', cls: styles.sentimentMix },
};

function cycleLabel(type: string) {
  return CYCLE_LABELS[type] ?? type;
}

function isCycleNegative(type: string) {
  return type.includes('CRISIS') || type.includes('TROUGH') || type.includes('CONTRACTION');
}

function NewsCard({ item }: { item: NewsItem }) {
  const sentiment = SENTIMENT_LABELS[item.economicCycleType?.toLowerCase()] ?? null;
  const cycleNeg = isCycleNegative(item.economicCycleType);

  return (
    <article className={styles.article}>
      <div className={styles.articleMeta}>
        <span className={`${styles.cycleBadge} ${cycleNeg ? styles.cycleBadgeNeg : styles.cycleBadgePos}`}>
          {cycleLabel(item.economicCycleType)}
        </span>
        {sentiment && (
          <span className={`${styles.sentimentBadge} ${sentiment.cls}`}>{sentiment.label}</span>
        )}
        <span className={styles.source}>{item.sourceName}</span>
        <span className={styles.date}>{item.publishedDate}</span>
      </div>
      <h2 className={styles.headline}>{item.headline}</h2>
      <p className={styles.content}>{item.content}</p>
    </article>
  );
}

export function NewsEventModal({ isOpen, onClose, sessionId }: Props) {
  const { news, loading, fetchLatestNews } = useGameWorld(sessionId);

  useEffect(() => {
    if (isOpen) fetchLatestNews();
  }, [isOpen, fetchLatestNews]);

  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        {/* 신문 헤더 */}
        <div className={styles.masthead}>
          <div className={styles.mastheadLine} />
          <div className={styles.mastheadTitle}>홈런 경제 신문</div>
          <div className={styles.mastheadSub}>
            {news ? `${news.currentDate} · ${news.turnNumber}번째 달` : '이달의 경제 뉴스'}
          </div>
          <div className={styles.mastheadLine} />
        </div>

        {/* 뉴스 목록 */}
        <div className={styles.body}>
          {loading && <div className={styles.loading}>뉴스를 불러오는 중...</div>}
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

export default NewsEventModal;
