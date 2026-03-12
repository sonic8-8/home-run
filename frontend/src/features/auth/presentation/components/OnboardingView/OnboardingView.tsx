import React from 'react';
import styles from './OnboardingView.module.css';

interface OnboardingViewProps {
  onKakaoLogin: () => void;
  onSsafyLogin: () => void;
  onEmailLogin: () => void;
}

export const OnboardingView: React.FC<OnboardingViewProps> = ({
  onKakaoLogin,
  onSsafyLogin,
  onEmailLogin,
}) => {
  return (
    <div className={styles.container}>
      <img
        className={styles.logo}
        src="/assets/images/logo.png"
        alt="HOME RUN"
      />
      <div className={styles.buttonGroup}>
        <button className={styles.kakaoButton} onClick={onKakaoLogin}>
          카카오 로그인
        </button>
        <button className={styles.ssafyButton} onClick={onSsafyLogin}>
          SSAFY 로그인
        </button>
        <button className={styles.emailText} onClick={onEmailLogin}>
          이메일 로그인
        </button>
      </div>
    </div>
  );
};
