import { useNavigate } from 'react-router-dom';
import backgroundImg from '@assets/images/background.png';
import logoImg from '@assets/images/logo.png';
import { ROUTES } from '@app/routes';
import styles from './GameStart.module.css';

export function GameStartPage() {
  const navigate = useNavigate();

  const handleContinue = () => {
    navigate(ROUTES.GAME_SAVE);
  };

  const handleNewGame = () => {
    navigate(ROUTES.GAME_SAVE);
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
          <button className={`${styles.button} ${styles.newGame}`} onClick={handleNewGame}>
            새로하기
          </button>
        </div>
      </div>
    </div>
  );
}
