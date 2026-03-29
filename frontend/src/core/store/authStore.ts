import { create } from 'zustand';
import { devtools, persist } from 'zustand/middleware';

interface AuthState {
  isAuthenticated: boolean;
  accessToken: string | null;
  refreshToken: string | null;
  nickname: string | null;
  accessTokenExpiresAt: number | null;
}

interface AuthActions {
  setAuth: (
    accessToken: string,
    refreshToken: string,
    accessTokenExpiresIn: number,
    nickname?: string,
  ) => void;
  setAccessToken: (token: string, accessTokenExpiresIn: number) => void;
  clearAuth: () => void;
}

const initialState: AuthState = {
  isAuthenticated: false,
  accessToken: null,
  refreshToken: null,
  nickname: null,
  accessTokenExpiresAt: null,
};

function toExpiresAt(accessTokenExpiresIn: number): number {
  return Date.now() + (accessTokenExpiresIn * 1000);
}

export const useAuthStore = create<AuthState & AuthActions>()(
  persist(
    devtools(
      (set) => ({
        ...initialState,
        setAuth: (
          accessToken,
          refreshToken,
          accessTokenExpiresIn,
          nickname,
        ) =>
          set({
            isAuthenticated: true,
            accessToken,
            refreshToken,
            nickname: nickname ?? null,
            accessTokenExpiresAt: toExpiresAt(accessTokenExpiresIn),
          }, false, 'auth/setAuth'),
        setAccessToken: (token, accessTokenExpiresIn) =>
          set({
            accessToken: token,
            accessTokenExpiresAt: toExpiresAt(accessTokenExpiresIn),
          }, false, 'auth/setAccessToken'),
        clearAuth: () => set(initialState, false, 'auth/clearAuth'),
      }),
      { name: 'AuthStore' },
    ),
    { name: 'auth' },
  ),
);
