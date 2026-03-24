import React, { useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';
import type { SignUpCredentials } from '../../../domain/entities/SignUpCredentials';
import styles from './SignUpForm.module.css';

interface SignUpFormProps {
  isLoading: boolean;
  error: string | null;
  onSubmit: (credentials: SignUpCredentials) => void;
}

export const SignUpForm: React.FC<SignUpFormProps> = ({
  isLoading,
  error,
  onSubmit,
}) => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showPasswordConfirm, setShowPasswordConfirm] = useState(false);
  const [agreed, setAgreed] = useState(false);
  const [validationError, setValidationError] = useState<string | null>(null);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (password !== passwordConfirm) {
      setValidationError('비밀번호가 일치하지 않습니다.');
      return;
    }
    setValidationError(null);
    onSubmit({
      name,
      email,
      password,
      passwordConfirm,
      termsAgreed: agreed,
    });
  };

  const displayError = validationError ?? error;

  return (
    <div className={styles.container}>
      <form onSubmit={handleSubmit}>
        <div className={styles.fieldGroup}>
          <label className={styles.label}>이름</label>
          <input
            className={styles.input}
            type="text"
            placeholder="김싸피"
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
        </div>

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
        </div>

        <div className={styles.fieldGroup}>
          <label className={styles.label}>비밀번호 확인하기</label>
          <div className={styles.passwordWrapper}>
            <input
              className={styles.input}
              type={showPasswordConfirm ? 'text' : 'password'}
              placeholder="Password"
              value={passwordConfirm}
              onChange={(e) => setPasswordConfirm(e.target.value)}
            />
            <button
              type="button"
              className={styles.eyeButton}
              aria-label={showPasswordConfirm ? '비밀번호 숨기기' : '비밀번호 보기'}
              onClick={() => setShowPasswordConfirm((prev) => !prev)}
            >
              {showPasswordConfirm ? <EyeOff size={20} /> : <Eye size={20} />}
            </button>
          </div>
        </div>

        <label className={styles.agreeRow}>
          <input
            type="checkbox"
            checked={agreed}
            onChange={(e) => setAgreed(e.target.checked)}
          />
          <span>회원 약관에 동의합니다.</span>
        </label>

        {displayError && <p className={styles.error}>{displayError}</p>}

        <button
          type="submit"
          className={styles.submitButton}
          disabled={isLoading || !agreed}
        >
          {isLoading ? '가입 중...' : '회원가입'}
        </button>
      </form>
    </div>
  );
};
