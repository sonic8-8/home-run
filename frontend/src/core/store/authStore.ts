import { create } from 'zustand';

interface AuthState {
  isAuthenticated: boolean;
  accessToken: string | null;
  refreshToken: string | null;
  nickname: string | null;
  setAuth: (accessToken: string, refreshToken: string, nickname?: string) => void;
  setAccessToken: (token: string) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>()((set) => ({
  isAuthenticated: false,
  accessToken: null,
  refreshToken: null,
  nickname: null,
  setAuth: (accessToken, refreshToken, nickname) =>
    set({ isAuthenticated: true, accessToken, refreshToken, nickname }),
  setAccessToken: (token) => set({ accessToken: token }),
  clearAuth: () => set({ isAuthenticated: false, accessToken: null, refreshToken: null, nickname: null }),
}));
