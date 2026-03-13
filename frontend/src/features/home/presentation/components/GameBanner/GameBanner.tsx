import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import styles from './GameBanner.module.css';

export const GameBanner: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className={styles.banner} onClick={() => navigate(ROUTES.GAME_START)}>
      <img src="/assets/images/icon.png" alt="홈런" className={styles.logo} />
      <div className={styles.text}>
        <div className={styles.sub}>게임으로 금융을 배운다.</div>
        <div className={styles.title}>GAME START</div>
      </div>
    </div>
  );
};
