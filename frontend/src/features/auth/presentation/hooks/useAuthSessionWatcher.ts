import { useEffect } from 'react';
import { apiClient } from '@core/network/apiClient';
import { refreshSession } from '@core/network/authSession';
import { expireSession } from '@core/network/sessionExpiry';
import { useAuthStore } from '@core/store/authStore';

const REFRESH_BUFFER_MS = 60_000;

export function useAuthSessionWatcher(): void {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const accessToken = useAuthStore((state) => state.accessToken);
  const accessTokenExpiresAt = useAuthStore((state) => state.accessTokenExpiresAt);
  const setAccessToken = useAuthStore((state) => state.setAccessToken);

  useEffect(() => {
    if (!isAuthenticated || accessToken === null) {
      return;
    }

    let isCancelled = false;
    const refreshDelay = accessTokenExpiresAt === null
      ? 0
      : Math.max(accessTokenExpiresAt - Date.now() - REFRESH_BUFFER_MS, 0);

    const timeoutId = window.setTimeout(() => {
      void refreshSession(apiClient.defaults.baseURL ?? '')
        .then((session) => {
          if (isCancelled) {
            return;
          }

          setAccessToken(session.accessToken, session.accessTokenExpiresIn);
        })
        .catch(() => {
          if (isCancelled) {
            return;
          }

          expireSession();
        });
    }, refreshDelay);

    return () => {
      isCancelled = true;
      window.clearTimeout(timeoutId);
    };
  }, [
    accessToken,
    accessTokenExpiresAt,
    isAuthenticated,
    setAccessToken,
  ]);
}
