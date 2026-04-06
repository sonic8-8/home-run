import clsx from 'clsx';
import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import styles from './SelectStartMethod.module.css';

type CharacterType = 'FEMALE' | 'MALE';
type StartMethod = 'mydata' | 'job';

interface LocationState {
  slotNumber?: number;
  characterType?: CharacterType;
  characterName?: string;
}

const CHAR_IMAGE: Record<CharacterType, string> = {
  FEMALE: '/assets/images/gcharac.png',
  MALE: '/assets/images/bcharac.png',
};

export function SelectStartMethodPage() {
  const [method, setMethod] = useState<StartMethod | null>(null);
  const navigate = useNavigate();
  const location = useLocation();
  const state = (location.state ?? {}) as LocationState;
  const characterType = state.characterType ?? 'MALE';

  const handleNext = () => {
    if (!method) return;
    if (method === 'mydata') {
      navigate(ROUTES.REAL_ESTATE_NEW_GAME, {
        state: { ...state, useMyData: true },
      });
      return;
    }

    navigate(ROUTES.GAME_SELECT_JOB, {
      state: { ...state, useMyData: false },
    });
  };

  return (
    <div className={styles.page}>
      <div className={styles.charSection}>
        <img
          src={CHAR_IMAGE[characterType]}
          alt={characterType === 'FEMALE' ? '여자 캐릭터' : '남자 캐릭터'}
          className={styles.charImg}
        />
      </div>

      <div className={styles.formSection} data-guide="game-start-method-options">
        <span className={styles.label}>시작 방법을 선택해주세요</span>

        <button
          className={clsx(styles.optionBtn, method === 'mydata' && styles.selected)}
          onClick={() => setMethod('mydata')}
        >
          자산 연결해서 시작하기
        </button>

        <button
          className={clsx(styles.optionBtn, method === 'job' && styles.selected)}
          onClick={() => setMethod('job')}
        >
          직업 선택하기
        </button>

        <button
          className={clsx(styles.nextBtn, method && styles.active)}
          onClick={handleNext}
          disabled={!method}
        >
          NEXT &gt;
        </button>
      </div>
    </div>
  );
}
