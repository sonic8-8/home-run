import { useState, useCallback } from 'react';
import type { LoginCredentials } from '../../domain/entities/LoginCredentials';
import type { SignUpCredentials } from '../../domain/entities/SignUpCredentials';

type AuthView = 'onboarding' | 'emailLogin' | 'signUp';

export const useAuth = () => {
  const [view, setView] = useState<AuthView>('onboarding');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const login = useCallback(async (_credentials: LoginCredentials) => {
    setIsLoading(true);
    setError(null);
    try {
      // TODO: container.resolve(LoginUseCase).execute(credentials) 구현 후 credentials 앞 _ 빼기
    } catch (e) {
      setError(e instanceof Error ? e.message : '로그인에 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  }, []);

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
