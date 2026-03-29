import { useCallback, useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { container } from '@core/di/container';
import { toErrorMessage } from '@core/error/AppError';
import { AUTH_SESSION_EXPIRED_KEY } from '@core/network/sessionExpiry';
import { useAuthStore } from '@core/store/authStore';
import { ROUTES } from '@app/routes';
import { AUTH_SESSION_EXPIRED_MESSAGE } from '@features/auth/presentation/constants/session';
import {
  readSessionStorage,
  removeSessionStorage,
} from '@shared/utils/sessionStorage';
import { LoginUseCase } from '@features/auth/domain/usecases/LoginUseCase';
import { SignUpUseCase } from '@features/auth/domain/usecases/SignUpUseCase';
import type { LoginCredentials } from '../../domain/entities/LoginCredentials';
import type { SignUpCredentials } from '../../domain/entities/SignUpCredentials';

type AuthView = 'onboarding' | 'emailLogin' | 'signUp';

interface AuthLocationState {
  from?: {
    pathname?: string;
  };
}

export const useAuth = () => {
  const [view, setView] = useState<AuthView>('emailLogin');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const location = useLocation();
  const setAuth = useAuthStore((s) => s.setAuth);

  useEffect(() => {
    if (readSessionStorage(AUTH_SESSION_EXPIRED_KEY) !== '1') {
      return;
    }

    removeSessionStorage(AUTH_SESSION_EXPIRED_KEY);
    setError(AUTH_SESSION_EXPIRED_MESSAGE);
  }, []);

  const login = useCallback(
    async (credentials: LoginCredentials) => {
      setIsLoading(true);
      setError(null);
      try {
        const loginUseCase = container.resolve(LoginUseCase);
        const data = await loginUseCase.execute(credentials);
        const from = (location.state as AuthLocationState | null)?.from?.pathname ?? ROUTES.HOME;
        setAuth(
          data.accessToken,
          data.refreshToken,
          data.accessTokenExpiresIn,
          data.name,
        );
        navigate(from, { replace: true });
      } catch (e) {
        setError(toErrorMessage(e));
      } finally {
        setIsLoading(false);
      }
    },
    [location.state, navigate, setAuth],
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
        setError(toErrorMessage(e));
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
