import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import type { CharacterType } from '@features/game/domain/entities/CharacterOption';
import { useSelectCharacter } from '../../hooks/useSelectCharacter';
import styles from './SelectCharacter.module.css';

const CHARACTER_IMAGES: Record<CharacterType, string> = {
  FEMALE: '/assets/images/gcharac.png',
  MALE: '/assets/images/bcharac.png',
};

interface LocationState {
  slotNumber?: number;
  isNew?: boolean;
}

export default function SelectCharacter() {
  const [selected, setSelected] = useState<CharacterType | null>(null);
  const navigate = useNavigate();
  const location = useLocation();
  const state = (location.state ?? {}) as LocationState;
  const { loading } = useSelectCharacter();

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
        {(['FEMALE', 'MALE'] as CharacterType[]).map((type, i) => (
          <>
            <button
              key={type}
              className={`${styles.charBtn} ${selected === type ? styles.selected : ''}`}
              onClick={() => setSelected(type)}
              aria-label={type === 'FEMALE' ? '여자 캐릭터 선택' : '남자 캐릭터 선택'}
            >
              <img src={CHARACTER_IMAGES[type]} alt={type} className={styles.charImg} />
              {selected === type && <span className={styles.selectedIndicator} />}
            </button>
            {i === 0 && (
              <button className={styles.nextBtn} onClick={handleNext} disabled={!selected}>
                NEXT
              </button>
            )}
          </>
        ))}
      </div>
    </div>
  );
}
