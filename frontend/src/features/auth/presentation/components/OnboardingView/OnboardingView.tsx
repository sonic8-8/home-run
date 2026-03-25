import React from 'react';
import styles from './OnboardingView.module.css';

interface OnboardingViewProps {
  onEmailLogin: () => void;
}

export const OnboardingView: React.FC<OnboardingViewProps> = ({
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
        <button className={styles.emailText} onClick={onEmailLogin}>
          이메일 로그인
        </button>
      </div>
    </div>
  );
};
