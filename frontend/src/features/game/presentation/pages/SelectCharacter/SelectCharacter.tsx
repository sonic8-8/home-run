import { Fragment, useState } from 'react';
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
        {characters.map((character, i) => (
          <Fragment key={character.characterType}>
            <button
              className={`${styles.charBtn} ${selected === character.characterType ? styles.selected : ''}`}
              onClick={() => setSelected(character.characterType)}
              aria-label={
                character.characterType === 'FEMALE'
                  ? '여자 캐릭터 선택'
                  : '남자 캐릭터 선택'
              }
            >
              <img
                src={character.thumbnailUrl || CHARACTER_IMAGES[character.characterType]}
                alt={character.characterType}
                className={styles.charImg}
              />
              {selected === character.characterType && <span className={styles.selectedIndicator} />}
            </button>
            {i === 0 && (
              <button className={styles.nextBtn} onClick={handleNext} disabled={!selected}>
                NEXT
              </button>
            )}
          </Fragment>
        ))}
      </div>
    </div>
  );
}
