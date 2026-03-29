import { useNavigate } from 'react-router-dom';
import backgroundImg from '@assets/images/background.png';
import logoImg from '@assets/images/logo.png';
import { ROUTES } from '@app/routes';
import type { GameSlot } from '@features/game/domain/entities/GameSlot';
import { useEndingArchivePage } from '@features/ending/presentation/hooks/useEndingArchivePage';
import styles from './EndingArchivePage.module.css';

const ENDING_TYPE_LABELS: Record<Exclude<GameSlot['status'], 'EMPTY' | 'IN_PROGRESS'>, string> = {
  CLEAR: '내 집 마련 성공',
  BANKRUPT: '파산',
  TIMEOUT: '기한 초과',
  FORECLOSURE: '차압',
};

const formatDate = (value?: string): string => {
  if (!value) {
    return '기록 없음';
  }

  const parsedDate = new Date(value);
  if (Number.isNaN(parsedDate.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  }).format(parsedDate);
};

const formatAssets = (value?: number): string => {
  if (value === undefined) {
    return '기록 없음';
  }

  return `${value.toLocaleString('ko-KR')}원`;
};

function EndingArchiveCard({
  ending,
  onOpen,
}: {
  ending: GameSlot;
  onOpen: (ending: GameSlot) => void;
}) {
  const endingLabel = ENDING_TYPE_LABELS[ending.status as Exclude<GameSlot['status'], 'EMPTY' | 'IN_PROGRESS'>];

  return (
    <button
      type="button"
      className={styles.card}
      onClick={() => onOpen(ending)}
      data-testid={`ending-archive-card-${ending.sessionId ?? ending.slotNumber}`}
    >
      <div className={styles.cardHeader}>
        <span className={styles.cardBadge}>{endingLabel}</span>
        <span className={styles.cardSlot}>SLOT {ending.slotNumber}</span>
      </div>
      <strong className={styles.cardName}>{ending.characterName ?? '이름 없는 플레이'}</strong>
      <p className={styles.cardMeta}>{formatDate(ending.createdAt)}</p>
      <p className={styles.cardMeta}>총자산 {formatAssets(ending.totalAssets)}</p>
    </button>
  );
}

export function EndingArchivePage() {
  const navigate = useNavigate();
  const {
    endings,
    isLoading,
    error,
    isEmpty,
    retryFetch,
  } = useEndingArchivePage();

  const handleBack = () => {
    navigate(ROUTES.GAME_START);
  };

  const handleOpenEnding = (ending: GameSlot) => {
    if (ending.sessionId === null) {
      return;
    }

    navigate(ROUTES.GAME_ENDING(ending.sessionId));
  };

  return (
    <div
      className={styles.page}
      style={{ backgroundImage: `url(${backgroundImg})` }}
      data-testid="ending-archive-page"
    >
      <div className={styles.overlay} />
      <div className={styles.content}>
        <div className={styles.header}>
          <img src={logoImg} alt="Home Run" className={styles.logo} />
          <button type="button" className={styles.backButton} onClick={handleBack}>
            게임 시작 화면으로
          </button>
        </div>

        <section className={styles.panel}>
          <div className={styles.panelHeader}>
            <div>
              <h1 className={styles.title}>엔딩 저장소</h1>
              <p className={styles.description}>이전에 완료한 엔딩을 다시 확인할 수 있습니다.</p>
            </div>
            <span className={styles.counter}>총 {endings.length}개</span>
          </div>

          {isLoading && <p className={styles.stateMessage}>엔딩 기록을 불러오는 중입니다.</p>}

          {error && (
            <div className={styles.stateBlock}>
              <p className={styles.stateMessage}>{error}</p>
              <button type="button" className={styles.retryButton} onClick={() => void retryFetch()}>
                다시 시도
              </button>
            </div>
          )}

          {isEmpty && (
            <div className={styles.stateBlock}>
              <p className={styles.stateMessage}>아직 저장된 엔딩이 없습니다.</p>
            </div>
          )}

          {!isLoading && !error && endings.length > 0 && (
            <div className={styles.cardList} data-testid="ending-archive-list">
              {endings.map((ending) => (
                <EndingArchiveCard
                  key={ending.sessionId ?? ending.slotNumber}
                  ending={ending}
                  onOpen={handleOpenEnding}
                />
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
