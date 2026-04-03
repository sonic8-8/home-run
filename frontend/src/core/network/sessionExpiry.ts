import { useAuthStore } from '@core/store/authStore';
import { removeSessionStorage, writeSessionStorage } from '@shared/utils/sessionStorage';

export const AUTH_SESSION_EXPIRED_KEY = 'auth/session-expired';

export function expireSession(): void {
  writeSessionStorage(AUTH_SESSION_EXPIRED_KEY, '1');
  removeSessionStorage('game:sessionId');
  removeSessionStorage('game:characterType');
  useAuthStore.getState().clearAuth();
}
