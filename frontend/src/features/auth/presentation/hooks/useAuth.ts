import { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { container } from '@core/di/container';
import { useAuthStore } from '@core/store/authStore';
import { ROUTES } from '@app/routes';
import { LoginUseCase } from '@features/auth/domain/usecases/LoginUseCase';
import { SignUpUseCase } from '@features/auth/domain/usecases/SignUpUseCase';
import type { LoginCredentials } from '../../domain/entities/LoginCredentials';
import type { SignUpCredentials } from '../../domain/entities/SignUpCredentials';

type AuthView = 'onboarding' | 'emailLogin' | 'signUp';

export const useAuth = () => {
  const [view, setView] = useState<AuthView>('emailLogin');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const setAuth = useAuthStore((s) => s.setAuth);

  const login = useCallback(
    async (credentials: LoginCredentials) => {
      setIsLoading(true);
      setError(null);
      try {
        const loginUseCase = container.resolve(LoginUseCase);
        const data = await loginUseCase.execute(credentials);
        setAuth(data.accessToken, data.refreshToken, data.name);
        navigate(ROUTES.HOME, { replace: true });
      } catch (e) {
        setError(e instanceof Error ? e.message : '로그인에 실패했습니다.');
      } finally {
        setIsLoading(false);
      }
    },
    [navigate, setAuth],
  );

  const signUp = useCallback(
    async (credentials: SignUpCredentials) => {
      setIsLoading(true);
      setError(null);
      try {
        const signUpUseCase = container.resolve(SignUpUseCase);
        await signUpUseCase.execute(credentials);
        setView('emailLogin');
      } catch (e) {
        setError(e instanceof Error ? e.message : '회원가입에 실패했습니다.');
      } finally {
        setIsLoading(false);
      }
    },
    [],
  );

  return {
    view,
    setView,
    isLoading,
    error,
    login,
    signUp,
  };
};
