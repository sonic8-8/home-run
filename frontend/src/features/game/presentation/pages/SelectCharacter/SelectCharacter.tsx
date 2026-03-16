import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import styles from './SelectCharacter.module.css';

type CharacterType = 'girl' | 'boy';

interface LocationState {
  slotNumber?: number;
  isNew?: boolean;
}

export default function SelectCharacter() {
  const [selected, setSelected] = useState<CharacterType | null>(null);
  const navigate = useNavigate();
  const location = useLocation();
  const state = (location.state ?? {}) as LocationState;

  const handleNext = () => {
    if (!selected) return;
    navigate(ROUTES.GAME_SET_NICKNAME, {
      state: { ...state, characterType: selected },
    });
  };

  return (
    <div className={styles.page}>
    
      <div className={styles.titleArea}>
        <h1 className={styles.title}>
          SELECT
          <br />
          CHARACTER
        </h1>
      </div>

      <div className={styles.selectArea}>
        <button
          className={`${styles.charBtn} ${selected === 'girl' ? styles.selected : ''}`}
          onClick={() => setSelected('girl')}
          aria-label="여자 캐릭터 선택"
        >
          <img src="/assets/images/gcharac.png" alt="여자 캐릭터" className={styles.charImg} />
          {selected === 'girl' && <span className={styles.selectedIndicator} />}
        </button>

        <button
          className={styles.nextBtn}
          onClick={handleNext}
          disabled={!selected}
        >
          NEXT
        </button>

        <button
          className={`${styles.charBtn} ${selected === 'boy' ? styles.selected : ''}`}
          onClick={() => setSelected('boy')}
          aria-label="남자 캐릭터 선택"
        >
          <img src="/assets/images/bcharac.png" alt="남자 캐릭터" className={styles.charImg} />
          {selected === 'boy' && <span className={styles.selectedIndicator} />}
        </button>
      </div>
    </div>
  );
}
