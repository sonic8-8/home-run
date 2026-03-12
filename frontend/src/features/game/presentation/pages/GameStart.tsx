import { useNavigate } from 'react-router-dom';
import backgroundImg from '@assets/images/background.png';
import logoImg from '@assets/images/logo.png';
import styles from './GameStart.module.css';

export default function GameStart() {
  const navigate = useNavigate();

  const handleContinue = () => {
    navigate('/game/play');
  };

  const handleNewGame = () => {
    navigate('/game/play', { state: { isNew: true } });
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
