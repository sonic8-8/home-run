import { useNavigate } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import type { CharacterType } from '@features/game/domain/entities/CharacterOption';
import { useGameMain } from '@features/game/presentation/hooks/useGameMain';
import { NewsEventModal } from '@features/game/presentation/components/NewsEventModal/NewsEventModal';
import { LoanProductsPanel } from '@features/game/presentation/components/LoanProductsPanel/LoanProductsPanel';
import { CardRecommendPanel } from '@features/game/presentation/components/CardRecommendPanel/CardRecommendPanel';
import { formatKoreanDate } from '@shared/utils/formatter';
import sceneRoad from '@assets/images/game_back_road.png';
import styles from './GameMainPage.module.css';

const CHARACTER_IMAGE: Record<CharacterType, string> = {
  MALE: '/assets/images/bcharac.png',
  FEMALE: '/assets/images/gcharac.png',
};

export function GameMainPage() {
  const navigate = useNavigate();
  const {
    sessionId,
    currentDate,
    characterType,
    news,
    isNewsLoading,
    newsError,
    isNewsOpen,
    openNews,
    closeNews,
    error,
    isLoading,
    leftView,
    setLeftView,
    preSelectedPropertyId,
    preSelectedPropertyName,
    preSelectedPropertyPrice,
  } = useGameMain();

  const characterImage = CHARACTER_IMAGE[characterType];

  if (isLoading && currentDate === null) {
    return (
      <div className={styles.page}>
        <div className={styles.container}>게임 정보를 불러오는 중입니다.</div>
      </div>
    );
  }

  if (error && currentDate === null) {
    return (
      <div className={styles.page}>
        <div className={styles.container}>{error}</div>
      </div>
    );
  }

  return (
    <>
      <div className={styles.page}>
        <div className={styles.container}>
          {leftView === 'scene' ? (
            <div
              className={styles.scene}
              style={{ backgroundImage: `url(${sceneRoad})` }}
            >
              <div className={styles.dateLabel}>
                <span className={styles.calendarIcon}>📅</span>
                {currentDate === null ? '' : formatKoreanDate(currentDate)}
              </div>
              <img src={characterImage} alt="캐릭터" className={styles.character} />
            </div>
          ) : (
            <div className={styles.leftPanel}>
              {leftView === 'loan' && (
                sessionId !== null ? (
                  <LoanProductsPanel
                    sessionId={sessionId}
                    preSelectedPropertyId={preSelectedPropertyId}
                    preSelectedPropertyName={preSelectedPropertyName}
                    preSelectedPropertyPrice={preSelectedPropertyPrice}
                  />
                ) : null
              )}
              {leftView === 'card' && <CardRecommendPanel />}
            </div>
          )}

          <div className={styles.panel}>
            <section className={styles.section}>
              <h2 className={styles.sectionTitle}>메뉴</h2>
              <div className={styles.menuList}>
                <button
                  className={leftView === 'loan' ? styles.menuButtonActive : styles.menuButton}
                  onClick={() => setLeftView(leftView === 'loan' ? 'scene' : 'loan')}
                >
                  대출 알아보기
                </button>
                <button
                  className={styles.menuButton}
                  onClick={() => {
                    if (sessionId === null) {
                      return;
                    }

                    navigate(ROUTES.REAL_ESTATE, {
                      state: { sessionId, mode: 'browse' },
                    });
                  }}
                >
                  부동산 알아보기
                </button>
                <button
                  className={leftView === 'card' ? styles.menuButtonActive : styles.menuButton}
                  onClick={() => setLeftView(leftView === 'card' ? 'scene' : 'card')}
                >
                  카드 추천
                </button>
                <button
                  className={styles.menuButton}
                  onClick={openNews}
                >
                  이달의 뉴스
                </button>
              </div>
            </section>
          </div>
        </div>
      </div>

      {sessionId !== null && (
        <NewsEventModal
          isOpen={isNewsOpen}
          onClose={closeNews}
          news={news}
          loading={isNewsLoading}
          error={newsError}
        />
      )}
    </>
  );
}
