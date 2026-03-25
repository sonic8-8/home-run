import { useState } from 'react';
import { AppHeader } from '../AppHeader/AppHeader';
import styles from './AssetLinkPage.module.css';

interface AssetLinkPageProps {
  onLink: () => Promise<void>;
}

export function AssetLinkPage({ onLink }: AssetLinkPageProps) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleLink = async () => {
    setLoading(true);
    setError(null);
    try {
      await onLink();
    } catch {
      setError('마이데이터 연동에 실패했습니다. 다시 시도해 주세요.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.page}>
      <AppHeader />
      <main className={styles.main}>
        <div className={styles.card}>
          <div className={styles.iconWrap}>🔗</div>
          <div className={styles.title}>마이데이터 연동이 필요합니다</div>
          <div className={styles.desc}>
            금융 데이터를 연동하면 개인화된 자산 관리와 맞춤 추천 서비스를 이용할 수 있습니다.
          </div>
          <ul className={styles.benefitList}>
            <li className={styles.benefitItem}>내 자산 현황 한눈에 확인</li>
            <li className={styles.benefitItem}>맞춤 카드 · 대출 상품 추천</li>
            <li className={styles.benefitItem}>시드머니 계좌 자동 생성</li>
            <li className={styles.benefitItem}>신용점수 기반 금융 분석</li>
          </ul>
          <button className={styles.linkBtn} onClick={handleLink} disabled={loading}>
            {loading ? '연동 중...' : '마이데이터 연동하기'}
          </button>
          {error && <div className={styles.error}>{error}</div>}
        </div>
      </main>
    </div>
  );
}
