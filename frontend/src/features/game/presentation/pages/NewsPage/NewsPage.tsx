import { useNavigate } from 'react-router-dom';
import { NewsArticle } from '@features/game/presentation/components/NewsArticle';
import { useGameNewsPage } from '@features/game/presentation/hooks/useGameNewsPage';
import { formatIsoDate } from '@shared/utils/formatter';
import styles from './NewsPage.module.css';

export function NewsPage() {
  const navigate = useNavigate();
  const {
    news,
    isNewsLoading,
    newsError,
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
      </main>
    </div>
  );
}
