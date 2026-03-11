# 🗃️ core/store 설계 원칙 (Zustand)

## Store가 필요한 이유

```
상황: LoanPage에서 로그인한 사용자의 이름을 표시하고 싶다

방법 1: Props로 내려주기 (Props Drilling)
  App → Router → LoanPage → LoanHeader → UserNameDisplay
  → 중간 컴포넌트들이 필요도 없는 user를 전달해야 함 ❌

방법 2: Zustand Store
  useAuthStore() → user 꺼내기
  → 어디서든 바로 접근 ✅
```

전역 상태가 필요한 데이터를 Store에 보관하고, 어느 컴포넌트(훅)에서든 꺼내 쓴다.

---

## 위치

```
core/store/
├── authStore.ts     # 로그인 사용자 정보, 인증 상태
├── gameStore.ts     # 게임 씬, 캐릭터, 게임 로딩
└── uiStore.ts       # 전역 Toast, 전역 로딩 스피너
```

### "이 상태는 Store에 두어야 하나?" 판단 기준

```
질문 1: 여러 feature에서 이 상태가 필요한가?
  → YES: Store 후보

질문 2: 앱 전체의 흐름에 영향을 미치는가? (로그인 여부, 게임 씬 등)
  → YES: Store 확정

질문 3: 한 feature 안에서만 쓰이는가?
  → YES: feature 훅의 useState로 충분

예시)
  현재 로그인한 유저   → authStore    (여러 feature에서 필요)
  게임 씬/캐릭터 상태  → gameStore    (앱 흐름에 영향)
  Toast 메시지        → uiStore      (어디서든 띄울 수 있어야 함)
  대출 신청 폼 데이터  → useLoan 훅   (loan feature 내부만)
  모달 열림 여부       → useModal 훅  (컴포넌트 로컬)
```

---

## Store 작성 규칙

1. **State(상태)** 와 **Actions(행동)** 타입을 반드시 분리 정의
2. **초기 상태**를 별도 상수로 분리 (reset 시 재사용)
3. **devtools** 미들웨어 항상 적용 (브라우저 Redux DevTools로 디버깅)
4. set의 세 번째 인자로 **액션 이름** 명시 (`'auth/setUser'` 형태)
5. **비즈니스 로직 금지** - Store는 상태 저장소일 뿐, 로직은 UseCase에

---

## authStore

```ts
// core/store/authStore.ts
import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import { User } from '@features/auth/domain/entities/User';

// ── 타입 정의 ──────────────────────────────────────────────────────────
interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
}

interface AuthActions {
  setUser: (user: User) => void;     // 로그인 성공 시
  clearUser: () => void;             // 로그아웃 시
}

// ── 초기 상태 (reset에서 재사용) ──────────────────────────────────────
const initialState: AuthState = {
  user: null,
  isAuthenticated: false,
};

// ── Store 생성 ─────────────────────────────────────────────────────────
export const useAuthStore = create<AuthState & AuthActions>()(
  devtools(
    (set) => ({
      ...initialState,

      setUser: (user) =>
        set(
          { user, isAuthenticated: true },
          false,            // 상태 병합 여부 (false = 병합)
          'auth/setUser'    // DevTools에 표시될 액션 이름
        ),

      clearUser: () =>
        set(initialState, false, 'auth/clearUser'),
    }),
    { name: 'AuthStore' }  // DevTools에서 보이는 스토어 이름
  )
);
```

---

## gameStore

```ts
// core/store/gameStore.ts
import { create } from 'zustand';
import { devtools, subscribeWithSelector } from 'zustand/middleware';
import { Character } from '@features/game/domain/entities/Character';
import { Scene } from '@features/game/domain/entities/Scene';
import { Position } from '@features/game/domain/entities/Position';

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
    // subscribeWithSelector: 특정 상태 변화만 구독할 때 필요 (씬 전환 감지 등)
    subscribeWithSelector((set, get) => ({
      ...initialState,

      setScene: (scene) =>
        set({ currentScene: scene }, false, 'game/setScene'),

      setCharacter: (character) =>
        set({ character }, false, 'game/setCharacter'),

      // 캐릭터의 position만 바꾸기 (나머지 character 필드는 유지)
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
        set(initialState, false, 'game/reset'),
    })),
    { name: 'GameStore' }
  )
);

// ── 씬 전환 감지 (subscribeWithSelector 덕분에 가능) ──────────────────
// 게임 씬이 바뀔 때만 실행 (다른 상태 변경엔 실행 안 됨)
useGameStore.subscribe(
  (state) => state.currentScene?.id,
  (sceneId) => {
    if (sceneId) console.log(`[Game] 씬 전환: ${sceneId}`);
  }
);
```

---

## uiStore

```ts
// core/store/uiStore.ts
import { create } from 'zustand';
import { devtools } from 'zustand/middleware';

export interface Toast {
  id: string;
  message: string;
  type: 'success' | 'error' | 'info' | 'warning';
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

export const useUIStore = create<UIState & UIActions>()(
  devtools(
    (set, get) => ({
      toasts: [],
      isGlobalLoading: false,

      showToast: (message, type) => {
        const id = crypto.randomUUID();
        set(
          { toasts: [...get().toasts, { id, message, type }] },
          false,
          'ui/showToast'
        );
        // 3초 뒤 자동 제거
        setTimeout(() => get().removeToast(id), 3000);
      },

      removeToast: (id) =>
        set(
          { toasts: get().toasts.filter((t) => t.id !== id) },
          false,
          'ui/removeToast'
        ),

      setGlobalLoading: (isGlobalLoading) =>
        set({ isGlobalLoading }, false, 'ui/setGlobalLoading'),
    }),
    { name: 'UIStore' }
  )
);
```

---

## Store 사용법 — 선택적 구독

```ts
// ✅ 필요한 값만 구독 (해당 값이 바뀔 때만 리렌더링)
const user = useAuthStore((s) => s.user);
const isAuthenticated = useAuthStore((s) => s.isAuthenticated);

// ✅ 액션만 가져오기 (상태 변경해도 리렌더링 없음)
const setUser = useAuthStore((s) => s.setUser);

// ❌ 전체 Store 구독 (Store 안의 뭐가 바뀌든 리렌더링 발생)
const store = useAuthStore();
const { user, setUser, clearUser } = useAuthStore();  // 이렇게 쓰면 안 됨
```

---

## 컴포넌트/훅에서 Store 접근 패턴

```ts
// features/auth/presentation/hooks/useAuth.ts
// Store는 훅에서 접근하고, 컴포넌트엔 props로 내려준다
export const useAuth = () => {
  const user = useAuthStore((s) => s.user);        // 읽기
  const setUser = useAuthStore((s) => s.setUser);  // 쓰기 (액션)

  const login = async (email: string, password: string) => {
    const useCase = container.resolve(LoginUseCase);
    const result = await useCase.execute({ email, password });
    setUser(result);   // UseCase 결과 → Store
  };

  return { user, login };
};

// ✅ 컴포넌트는 Store를 직접 건드리지 않음
const LoginForm: React.FC = () => {
  const { user, login } = useAuth();  // 훅에서만 접근
  ...
};
```

---

## 미들웨어 선택 기준

| 미들웨어 | 언제 사용 | 예시 |
|---------|----------|------|
| `devtools` | **모든 Store** (개발 디버깅용) | 전체 적용 |
| `subscribeWithSelector` | 특정 상태 변화 감지가 필요할 때 | gameStore (씬 전환 감지) |
| `persist` | 새로고침 후에도 유지해야 할 때 | 사용자 설정, 게임 자동저장 |
| `immer` | 깊이 중첩된 객체 업데이트가 많을 때 | 복잡한 게임 맵 데이터 |

---

## 금지 패턴

```ts
// ❌ Store Action에서 비즈니스 로직 처리
export const useAuthStore = create((set) => ({
  login: async (email, password) => {
    const res = await axios.post('/auth/login', { email, password });  // UseCase 역할 침범
    set({ user: res.data.user });
  },
}));

// ❌ 상태 직접 변경 (불변성 위반)
useGameStore.getState().character.position.x = 100;  // set() 사용할 것

// ❌ Store를 컴포넌트에서 직접 구독
const LoanForm: React.FC = () => {
  const user = useAuthStore((s) => s.user);  // 훅에서 처리하고 props로 받을 것
};
```
