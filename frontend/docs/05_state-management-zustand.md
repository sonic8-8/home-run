# 🗃️ 상태 관리 설계 원칙 (Zustand)

## 개요

**Zustand**를 전역 상태 관리에 사용한다.  
단, 모든 상태를 전역으로 올리지 않는다.  
**"상태가 어디에 있어야 하는가"** 를 먼저 판단하고, 전역은 최소화한다.

---

## 상태 위치 결정 원칙

```
로컬 상태 (useState)
  → 단일 컴포넌트에서만 사용 (폼 입력값, 토글, 모달 열림 여부)

Feature 훅 상태 (useXxx 커스텀 훅)
  → 하나의 Feature 안에서만 공유 (대출 신청 폼 데이터, 캐릭터 임시 이동)

Zustand 전역 상태 (core/store/)
  → 여러 Feature에 걸쳐 공유되는 상태 (게임 씬 상태, 인증 사용자, 토스트 알림)
```

---

## Store 파일 위치

```
core/store/
├── gameStore.ts          # 게임 씬 전역 상태
├── authStore.ts          # 인증 상태 (로그인 유저 정보)
└── uiStore.ts            # 전역 UI 상태 (토스트, 글로벌 로딩)
```

> feature 내부에서만 쓰이는 상태는 `features/{feature}/presentation/hooks/` 에 위치.

---

## Store 기본 구조

```ts
// core/store/authStore.ts
import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import { User } from '@features/auth/domain/entities/User';

// 1. State 타입 정의
interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
}

// 2. Action 타입 정의 (State와 분리)
interface AuthActions {
  setUser: (user: User) => void;
  clearUser: () => void;
}

// 3. 초기 상태
const initialState: AuthState = {
  user: null,
  isAuthenticated: false,
};

// 4. Store 생성
export const useAuthStore = create<AuthState & AuthActions>()(
  devtools(
    (set) => ({
      ...initialState,

      setUser: (user) =>
        set({ user, isAuthenticated: true }, false, 'auth/setUser'),

      clearUser: () =>
        set({ ...initialState }, false, 'auth/clearUser'),
    }),
    { name: 'AuthStore' }   // DevTools 표시 이름
  )
);
```

---

## 게임 Store 예시

```ts
// core/store/gameStore.ts
import { create } from 'zustand';
import { devtools, subscribeWithSelector } from 'zustand/middleware';
import { Scene } from '@features/game/domain/entities/Scene';
import { Character } from '@features/game/domain/entities/Character';

interface GameState {
  currentScene: Scene | null;
  character: Character | null;
  isGameLoading: boolean;
  gameError: string | null;
}

interface GameActions {
  setScene: (scene: Scene) => void;
  setCharacter: (character: Character) => void;
  updateCharacterPosition: (position: Position) => void;
  setGameLoading: (loading: boolean) => void;
  setGameError: (error: string | null) => void;
  resetGame: () => void;
}

const initialState: GameState = {
  currentScene: null,
  character: null,
  isGameLoading: false,
  gameError: null,
};

export const useGameStore = create<GameState & GameActions>()(
  devtools(
    subscribeWithSelector((set, get) => ({
      ...initialState,

      setScene: (scene) =>
        set({ currentScene: scene }, false, 'game/setScene'),

      setCharacter: (character) =>
        set({ character }, false, 'game/setCharacter'),

      updateCharacterPosition: (position) => {
        const { character } = get();
        if (!character) return;
        set(
          { character: { ...character, position } },
          false,
          'game/updateCharacterPosition'
        );
      },

      setGameLoading: (isGameLoading) =>
        set({ isGameLoading }, false, 'game/setLoading'),

      setGameError: (gameError) =>
        set({ gameError }, false, 'game/setError'),

      resetGame: () =>
        set({ ...initialState }, false, 'game/reset'),
    })),
    { name: 'GameStore' }
  )
);
```

---

## UI Store (전역 알림, 로딩)

```ts
// core/store/uiStore.ts
import { create } from 'zustand';

interface Toast {
  id: string;
  message: string;
  type: 'success' | 'error' | 'info';
}

interface UIState {
  toasts: Toast[];
  isGlobalLoading: boolean;
}

interface UIActions {
  showToast: (message: string, type: Toast['type']) => void;
  removeToast: (id: string) => void;
  setGlobalLoading: (loading: boolean) => void;
}

export const useUIStore = create<UIState & UIActions>()((set, get) => ({
  toasts: [],
  isGlobalLoading: false,

  showToast: (message, type) => {
    const id = crypto.randomUUID();
    set({ toasts: [...get().toasts, { id, message, type }] });
    // 3초 후 자동 제거
    setTimeout(() => get().removeToast(id), 3000);
  },

  removeToast: (id) =>
    set({ toasts: get().toasts.filter((t) => t.id !== id) }),

  setGlobalLoading: (isGlobalLoading) => set({ isGlobalLoading }),
}));
```

---

## Store 구독 - 선택적 리렌더링

```ts
// ✅ 필요한 상태만 구독 (불필요한 리렌더링 방지)
const user = useAuthStore((state) => state.user);
const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

// ✅ 액션만 가져오기 (상태 변경 시 리렌더링 없음)
const setUser = useAuthStore((state) => state.setUser);

// ❌ 전체 스토어 구독 (모든 변경에 리렌더링)
const store = useAuthStore();
```

---

## Store와 UseCase 연계 패턴

Store는 UseCase에서 직접 건드리지 않는다.  
**Presentation 훅**이 UseCase 결과를 받아 Store를 업데이트한다.

```ts
// features/auth/presentation/hooks/useAuth.ts
import { container } from '@core/di/container';
import { useAuthStore } from '@core/store/authStore';
import { LoginUseCase } from '../../domain/usecases/LoginUseCase';

export const useAuth = () => {
  const setUser = useAuthStore((state) => state.setUser);
  const clearUser = useAuthStore((state) => state.clearUser);

  const login = async (email: string, password: string) => {
    const useCase = container.resolve(LoginUseCase);
    const user = await useCase.execute({ email, password });

    // UseCase 결과 → Store 업데이트
    setUser(user);
  };

  const logout = async () => {
    const useCase = container.resolve(LogoutUseCase);
    await useCase.execute();
    clearUser();
  };

  return { login, logout };
};
```

---

## subscribeWithSelector - 씬 전환 감지

```ts
// 게임 씬이 바뀔 때만 특정 동작 수행
import { useGameStore } from '@core/store/gameStore';

useGameStore.subscribe(
  (state) => state.currentScene,    // 감시할 상태
  (scene) => {
    if (scene?.id === 'main-lobby') {
      console.log('로비 씬 진입');
      // BGM 변경, 리소스 로드 등
    }
  }
);
```

---

## 미들웨어 사용 기준

| 미들웨어 | 사용 시점 |
|---------|----------|
| `devtools` | 개발 편의용, 모든 Store에 적용 권장 |
| `subscribeWithSelector` | 특정 상태 변화 구독이 필요한 Store (게임 등) |
| `persist` | 새로고침 후에도 유지해야 하는 상태 (사용자 설정 등) |
| `immer` | 깊은 중첩 상태 업데이트가 자주 필요한 경우 |

---

## 금지 패턴

```ts
// ❌ 직접 State 변경 (불변성 위반)
useGameStore.getState().character.position = { x: 0, y: 0 };

// ❌ Store에서 비즈니스 로직 처리
export const useAuthStore = create((set) => ({
  login: async (email, password) => {
    const res = await axios.post('/auth/login', { email, password });  // ← 금지
    set({ user: res.data });
  },
}));

// ❌ feature 간 Store 직접 교차 사용
// features/loan/에서 useGameStore 직접 구독 (공통 관심사면 uiStore로 올릴 것)
```
