import { create } from 'zustand';

interface AuthState {
  isAuthenticated: boolean;
  accessToken: string | null;
  nickname: string | null;
  setAccessToken: (token: string, nickname?: string) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>()((set) => ({
  isAuthenticated: false,
  accessToken: null,
  nickname: null,
  setAccessToken: (token, nickname = undefined) =>
    set({ isAuthenticated: true, accessToken: token, nickname }),
  clearAuth: () => set({ isAuthenticated: false, accessToken: null, nickname: null }),
}));
