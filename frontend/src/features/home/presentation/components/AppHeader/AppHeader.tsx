import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronDown } from 'lucide-react';
import { useAuthStore } from '@core/store/authStore';
import { ROUTES } from '@app/routes';
import styles from './AppHeader.module.css';

export const AppHeader: React.FC = () => {
  const nickname = useAuthStore((s) => s.nickname);
  const clearAuth = useAuthStore((s) => s.clearAuth);
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = () => {
    clearAuth();
    navigate(ROUTES.LOGIN, { replace: true });
  };

  return (
    <header className={styles.header}>
      <div className={styles.inner}>
        <div className={styles.logo}>
          <img src="/assets/images/icon.png" alt="홈런" className={styles.logoImg} />
          <span className={styles.logoText}>홈런</span>
        </div>
        <div className={styles.userArea} ref={dropdownRef}>
          <button
            className={styles.userButton}
            onClick={() => setDropdownOpen((prev) => !prev)}
          >
            <span>{nickname ?? '사용자'}</span>
            <ChevronDown size={16} className={dropdownOpen ? styles.chevronOpen : styles.chevron} />
          </button>
          {dropdownOpen && (
            <div className={styles.dropdown}>
              <button className={styles.dropdownItem} onClick={handleLogout}>
                로그아웃
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
