import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import type { CharacterType } from '@features/game/domain/entities/CharacterOption';
import { useSelectCharacter } from '../../hooks/useSelectCharacter';
import styles from './SelectCharacter.module.css';

interface LocationState {
  slotNumber?: number;
  isNew?: boolean;
}

export default function SelectCharacter() {
  const [selected, setSelected] = useState<CharacterType | null>(null);
  const navigate = useNavigate();
  const location = useLocation();
  const state = (location.state ?? {}) as LocationState;
  const { characters, loading } = useSelectCharacter();

  const handleNext = () => {
    if (!selected) return;
    navigate(ROUTES.GAME_SET_NICKNAME, {
      state: { ...state, characterType: selected },
    });
  };

  if (loading) return null;

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
        {characters.map((char) => (
          <button
            key={char.characterType}
            className={`${styles.charBtn} ${selected === char.characterType ? styles.selected : ''}`}
            onClick={() => setSelected(char.characterType)}
            aria-label={`${char.characterType} 캐릭터 선택`}
          >
            <img src={char.thumbnailUrl} alt={char.characterType} className={styles.charImg} />
            {selected === char.characterType && <span className={styles.selectedIndicator} />}
          </button>
        ))}

        <button
          className={styles.nextBtn}
          onClick={handleNext}
          disabled={!selected}
        >
          NEXT
        </button>
      </div>
    </div>
  );
}
