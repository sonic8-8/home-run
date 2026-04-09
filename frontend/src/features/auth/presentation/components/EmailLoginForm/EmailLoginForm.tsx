import React, { useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';
import type { LoginCredentials } from '../../../domain/entities/LoginCredentials';
import styles from './EmailLoginForm.module.css';

interface EmailLoginFormProps {
  isLoading: boolean;
  error: string | null;
  onSubmit: (credentials: LoginCredentials) => void;
  onSignUp: () => void;
}

export const EmailLoginForm: React.FC<EmailLoginFormProps> = ({
  isLoading,
  error,
  onSubmit,
  onSignUp,
}) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  return (
    <div className={styles.container}>
      <img
        className={styles.logo}
        src="/assets/images/logo.png"
        alt="HOME RUN"
      />

      <form
        onSubmit={(e) => {
          e.preventDefault();
          onSubmit({ email, password });
        }}
      >
        <div className={styles.fieldGroup}>
          <label className={styles.label}>이메일</label>
          <input
            className={styles.input}
            type="email"
            placeholder="example@email.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>

        <div className={styles.fieldGroup}>
          <label className={styles.label}>비밀번호</label>
          <div className={styles.passwordWrapper}>
            <input
              className={styles.input}
              type={showPassword ? 'text' : 'password'}
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            <button
              type="button"
              className={styles.eyeButton}
              aria-label={showPassword ? '비밀번호 숨기기' : '비밀번호 보기'}
              onClick={() => setShowPassword((prev) => !prev)}
            >
              {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
            </button>
          </div>
          <p className={styles.forgotPasswordHint}>
            비밀번호 찾기는 아직 준비 중입니다.
          </p>
        </div>

        {error && <p className={styles.error}>{error}</p>}

        <button
          type="submit"
          className={styles.submitButton}
          disabled={isLoading}
        >
          {isLoading ? '로그인 중...' : '로그인'}
        </button>
      </form>

      <button type="button" className={styles.signUpText} onClick={onSignUp}>
        이메일로 회원가입하기
      </button>
    </div>
  );
};
