import clsx from 'clsx';
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

export function SelectCharacterPage() {
  const [selected, setSelected] = useState<CharacterType | null>(null);
  const [brokenThumbnails, setBrokenThumbnails] = useState<Partial<Record<CharacterType, boolean>>>({});
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

      <div className={styles.selectArea} data-guide="game-character-options">
        {characters.map((character, i) => {
          const fallbackImage = CHARACTER_IMAGES[character.characterType];
          const imageSrc =
            brokenThumbnails[character.characterType] === true
              ? fallbackImage
              : character.thumbnailUrl || fallbackImage;

          return (
            <Fragment key={character.characterType}>
              <button
                className={clsx(
                  styles.charBtn,
                  selected === character.characterType && styles.selected,
                )}
                onClick={() => setSelected(character.characterType)}
                aria-label={
                  character.characterType === 'FEMALE'
                    ? '여자 캐릭터 선택'
                    : '남자 캐릭터 선택'
                }
              >
                <img
                  src={imageSrc}
                  alt={character.characterType}
                  className={styles.charImg}
                  onError={() => {
                    setBrokenThumbnails((current) => {
                      if (current[character.characterType] === true) {
                        return current;
                      }

                      return {
                        ...current,
                        [character.characterType]: true,
                      };
                    });
                  }}
                />
                {selected === character.characterType && <span className={styles.selectedIndicator} />}
              </button>
              {i === 0 && (
                <button className={styles.nextBtn} onClick={handleNext} disabled={!selected}>
                  NEXT
                </button>
              )}
            </Fragment>
          );
        })}
      </div>
    </div>
  );
}
