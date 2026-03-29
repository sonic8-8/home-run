import { useNavigate } from 'react-router-dom';
import { NewsArticle } from '@features/game/presentation/components/NewsArticle';
import { useGameNewsPage } from '@features/game/presentation/hooks/useGameNewsPage';
import { formatIsoDate } from '@shared/utils/formatter';
import styles from './NewsPage.module.css';

export function NewsPage() {
  const navigate = useNavigate();
  const {
    news,
    newsHistory,
    isNewsLoading,
    newsError,
    isNewsHistoryLoading,
    newsHistoryError,
    hasValidSessionId,
  } = useGameNewsPage();

  if (!hasValidSessionId) {
    return null;
  }

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <button className={styles.backBtn} onClick={() => navigate(-1)}>← 돌아가기</button>
        <div className={styles.headerCenter}>
          <div className={styles.headerTitle}>홈런 경제 신문</div>
          {news && (
            <div className={styles.headerSub}>
              {formatIsoDate(news.currentDate)} · {news.turnNumber}번째 달
            </div>
          )}
        </div>
        <div className={styles.headerRight} />
      </header>

      <div className={styles.mastheadLine} />

      <main className={styles.main}>
        {isNewsLoading && <div className={styles.status}>뉴스를 불러오는 중...</div>}
        {newsError && <div className={`${styles.status} ${styles.statusError}`}>{newsError}</div>}
        {!isNewsLoading && news && news.news.length === 0 && (
          <div className={styles.status}>이달 뉴스가 없습니다.</div>
        )}
        {!isNewsLoading && news && news.news.map((item) => (
          <NewsArticle key={item.newsId} item={item} />
        ))}

        <section className={styles.historySection}>
          <div className={styles.sectionHeader}>
            <h2 className={styles.sectionTitle}>지난 턴 헤드라인</h2>
            <p className={styles.sectionDescription}>
              이전 턴에 노출된 핵심 뉴스 제목을 빠르게 확인할 수 있습니다.
            </p>
          </div>
          {isNewsHistoryLoading && (
            <div className={styles.status}>지난 뉴스를 불러오는 중...</div>
          )}
          {newsHistoryError && (
            <div className={`${styles.status} ${styles.statusError}`}>{newsHistoryError}</div>
          )}
          {!isNewsHistoryLoading && !newsHistoryError && newsHistory.length === 0 && (
            <div className={styles.status}>저장된 뉴스 히스토리가 없습니다.</div>
          )}
          {!isNewsHistoryLoading && !newsHistoryError && newsHistory.length > 0 && (
            <ul className={styles.historyList}>
              {newsHistory.map((item) => (
                <li
                  key={`${item.turnNumber}-${item.newsId}`}
                  className={styles.historyItem}
                >
                  <div className={styles.historyTurn}>{item.turnNumber}번째 달</div>
                  <div className={styles.historyContent}>
                    <strong className={styles.historyHeadline}>{item.headline}</strong>
                    <span className={styles.historyMeta}>
                      {item.newsId} · {formatIsoDate(item.publishedDate)}
                    </span>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </section>
      </main>
    </div>
  );
}
