import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import styles from './GameBanner.module.css';

interface GameBannerProps {
  activeGameSessionId: number | null;
}

export const GameBanner: React.FC<GameBannerProps> = ({
  activeGameSessionId,
}) => {
  const navigate = useNavigate();

  return (
    <div className={styles.banner}>
      <img src="/assets/images/icon.png" alt="홈런" className={styles.logo} />
      <div className={styles.text}>
        <div className={styles.sub}>
          {activeGameSessionId !== null
            ? '현재 진행 중인 게임으로 바로 돌아갈 수 있습니다.'
            : '게임으로 금융을 배운다.'}
        </div>
        <div className={styles.title}>{activeGameSessionId !== null ? 'RESUME RUN' : 'GAME START'}</div>
        <div className={styles.actions}>
          {activeGameSessionId !== null ? (
            <>
              <button
                type="button"
                className={styles.primaryButton}
                onClick={() => navigate(ROUTES.GAME, { state: { sessionId: activeGameSessionId } })}
              >
                현재 세션 이어하기
              </button>
              <button
                type="button"
                className={styles.secondaryButton}
                onClick={() => navigate(ROUTES.GAME_START)}
              >
                시작 화면으로
              </button>
            </>
          ) : (
            <button
              type="button"
              className={styles.primaryButton}
              onClick={() => navigate(ROUTES.GAME_START)}
            >
              게임 시작하기
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
