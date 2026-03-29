import { useNavigate } from 'react-router-dom';
import backgroundImg from '@assets/images/background.png';
import logoImg from '@assets/images/logo.png';
import { ROUTES } from '@app/routes';
import { useGameGuide } from '@features/game/presentation/hooks/useGameGuide';
import styles from './GameStart.module.css';

export function GameStartPage() {
  const navigate = useNavigate();
  const { getTriggerLabel, toggleFlow } = useGameGuide();

  const handleContinue = () => {
    navigate(ROUTES.GAME_SAVE_WITH_MODE('continue'), {
      state: { entryMode: 'continue' as const },
    });
  };

  const handleNewGame = () => {
    navigate(ROUTES.GAME_SAVE_WITH_MODE('new'), {
      state: { entryMode: 'new' as const },
    });
  };

  const handleGuide = () => toggleFlow('start');

  const handleEndingArchive = () => {
    navigate(ROUTES.GAME_ENDING_ARCHIVE);
  };

  return (
    <div
      className={styles.page}
      style={{ backgroundImage: `url(${backgroundImg})` }}
    >
      <div className={styles.overlay} />
      <div className={styles.content}>
        <img src={logoImg} alt="Home Run" className={styles.logo} />
        <div className={styles.buttons}>
          <button className={`${styles.button} ${styles.continue}`} onClick={handleContinue}>
            이어하기
          </button>
          <button
            className={`${styles.button} ${styles.newGame}`}
            onClick={handleNewGame}
            data-guide="game-start-new"
          >
            새로하기
          </button>
          <button className={`${styles.button} ${styles.archive}`} onClick={handleEndingArchive}>
            엔딩 저장소
          </button>
          <button className={`${styles.button} ${styles.guide}`} onClick={handleGuide}>
            {getTriggerLabel('start')}
          </button>
        </div>
      </div>
    </div>
  );
}
