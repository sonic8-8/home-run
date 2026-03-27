import { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useGameWorld } from '@features/game/presentation/hooks/useGameWorld';
import type { NewsItem } from '@features/game/domain/entities/GameTurn';
import styles from './NewsPage.module.css';

interface LocationState {
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

function cycleLabel(type: string) {
  return CYCLE_LABELS[type] ?? type;
}

function isCycleNegative(type: string) {
  return type.includes('CRISIS') || type.includes('TROUGH') || type.includes('CONTRACTION');
}

function NewsArticle({ item }: { item: NewsItem }) {
  const [expanded, setExpanded] = useState(false);
  const cycleNeg = isCycleNegative(item.economicCycleType);

  return (
    <div className={styles.card} onClick={() => setExpanded((v) => !v)}>
      <div className={styles.cardMeta}>
        <span className={`${styles.cycleBadge} ${cycleNeg ? styles.cycleNeg : styles.cyclePos}`}>
          {cycleLabel(item.economicCycleType)}
        </span>
        <span className={styles.source}>{item.sourceName}</span>
        <span className={styles.date}>{item.publishedDate}</span>
      </div>
      <h3 className={styles.headline}>{item.headline}</h3>
      {expanded && (
        <p className={styles.content}>{item.content}</p>
      )}
      <button className={styles.toggleBtn} type="button">
        {expanded ? '접기 ▲' : '본문 보기 ▼'}
      </button>
    </div>
  );
}

export function NewsPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { sessionId } = (location.state as LocationState) ?? { sessionId: 0 };

  const { news, loading, error, fetchLatestNews } = useGameWorld(sessionId);

  useEffect(() => {
    fetchLatestNews();
  }, [fetchLatestNews]);

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <button className={styles.backBtn} onClick={() => navigate(-1)}>← 돌아가기</button>
        <div className={styles.headerCenter}>
          <div className={styles.headerTitle}>홈런 경제 신문</div>
          {news && (
            <div className={styles.headerSub}>
              {news.currentDate} · {news.turnNumber}번째 달
            </div>
          )}
        </div>
        <div className={styles.headerRight} />
      </header>

      <div className={styles.mastheadLine} />

      <main className={styles.main}>
        {loading && <div className={styles.status}>뉴스를 불러오는 중...</div>}
        {error && <div className={`${styles.status} ${styles.statusError}`}>{error}</div>}
        {!loading && news && news.news.length === 0 && (
          <div className={styles.status}>이달 뉴스가 없습니다.</div>
        )}
        {!loading && news && news.news.map((item) => (
          <NewsArticle key={item.newsId} item={item} />
        ))}
      </main>
    </div>
  );
}

export default NewsPage;
