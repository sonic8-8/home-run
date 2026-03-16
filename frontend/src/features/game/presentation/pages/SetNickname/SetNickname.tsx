import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import styles from './SetNickname.module.css';

type CharacterType = 'girl' | 'boy';

interface LocationState {
  slotNumber?: number;
  characterType?: CharacterType;
}

const CHAR_IMAGE: Record<CharacterType, string> = {
  girl: '/assets/images/gcharac.png',
  boy: '/assets/images/bcharac.png',
};

export default function SetNickname() {
  const [nickname, setNickname] = useState('');
  const navigate = useNavigate();
  const location = useLocation();
  const state = (location.state ?? {}) as LocationState;
  const characterType = state.characterType ?? 'boy';

  const isValid = nickname.trim().length > 0;

  const handleNext = () => {
    if (!isValid) return;
    navigate(ROUTES.GAME_SELECT_START_METHOD, {
      state: { ...state, characterName: nickname.trim() },
    });
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') handleNext();
  };

  return (
    <div className={styles.page}>
      <div className={styles.charSection}>
        <img
          src={CHAR_IMAGE[characterType]}
          alt={characterType === 'girl' ? '여자 캐릭터' : '남자 캐릭터'}
          className={styles.charImg}
        />
      </div>

      <div className={styles.formSection}>
        <span className={styles.label}>닉네임 설정</span>
        <input
          className={styles.input}
          type="text"
          placeholder="닉네임을 입력해주세요."
          value={nickname}
          onChange={(e) => setNickname(e.target.value)}
          onKeyDown={handleKeyDown}
          maxLength={10}
          autoFocus
        />
        <button
          className={`${styles.nextBtn} ${isValid ? styles.active : ''}`}
          onClick={handleNext}
          disabled={!isValid}
        >
          NEXT &gt;
        </button>
      </div>
    </div>
  );
}
