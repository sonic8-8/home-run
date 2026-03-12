import { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@core/store/authStore';
import { ROUTES } from '@app/routes';
import type { LoginCredentials } from '../../domain/entities/LoginCredentials';
import type { SignUpCredentials } from '../../domain/entities/SignUpCredentials';

type AuthView = 'onboarding' | 'emailLogin' | 'signUp';

export const useAuth = () => {
  const [view, setView] = useState<AuthView>('onboarding');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const setAccessToken = useAuthStore((s) => s.setAccessToken);

  const login = useCallback(
    async (_credentials: LoginCredentials) => {
      setIsLoading(true);
      setError(null);
      try {
        // TODO: 실제 API 연동 시 container.resolve(LoginUseCase).execute(credentials) 로 교체
        // 백엔드 미구현 — mock 처리
        setAccessToken('mock-token', '김싸피');
        navigate(ROUTES.HOME, { replace: true });
      } catch (e) {
        setError(e instanceof Error ? e.message : '로그인에 실패했습니다.');
      } finally {
        setIsLoading(false);
      }
    },
    [navigate, setAccessToken],
  );

  const signUp = useCallback(async (_credentials: SignUpCredentials) => {
    setIsLoading(true);
    setError(null);
    try {
      // TODO: container.resolve(SignUpUseCase).execute(credentials)
    } catch (e) {
      setError(e instanceof Error ? e.message : '회원가입에 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  return {
    view,
    setView,
    isLoading,
    error,
    login,
    signUp,
  };
};
