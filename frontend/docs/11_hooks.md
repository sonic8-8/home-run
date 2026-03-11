# 🪝 커스텀 훅 설계 원칙

## 훅이란?

React에서 `use`로 시작하는 함수를 **커스텀 훅**이라고 한다.  
컴포넌트에서 반복되는 로직을 **훅으로 분리**하면 컴포넌트는 UI 렌더링에만 집중할 수 있다.

```
컴포넌트가 하면 안 되는 것들
  ├── API 호출 (UseCase 경유해야 함)
  ├── 전역 상태 구독/변경 (Store)
  ├── 복잡한 상태 조합
  └── 부수효과 (데이터 fetch, 이벤트 리스너 등)

            ↓ 전부 훅으로 분리

컴포넌트가 하는 것
  └── 훅에서 받은 값으로 JSX 렌더링만
```

---

## shared 훅 vs feature 훅 — 핵심 차이

> **"이 훅 코드에 도메인 단어(대출, 게임, 인증...)가 등장하는가?"**
> - 등장한다 → `features/{feature}/presentation/hooks/`
> - 등장하지 않는다 → `shared/hooks/`

| 비교 항목 | shared 훅 | feature 훅 |
|----------|-----------|-----------|
| 위치 | `shared/hooks/` | `features/{f}/presentation/hooks/` |
| 알고 있는 것 | UI 패턴만 (모달, 디바운스...) | 도메인 개념 (대출, 게임, 인증...) |
| UseCase 호출 | ❌ 절대 없음 | ✅ 반드시 여기서 |
| Store 사용 | ❌ 없음 | ✅ 필요 시 |
| 재사용 범위 | 프로젝트 전체 | 해당 feature 내부만 |

### 판단 예시

```
useModal()         → 모달 열고 닫는 것, 도메인 단어 없음    → shared/hooks/
useDebounce()      → 입력 지연 처리, 도메인 단어 없음       → shared/hooks/
useWindowSize()    → 창 크기, 도메인 단어 없음              → shared/hooks/

useAuth()          → "인증"이라는 도메인 개념              → features/auth/
useLoan()          → "대출"이라는 도메인 개념              → features/loan/
useGame()          → "게임"이라는 도메인 개념              → features/game/
```

---

## 위치

```
shared/hooks/
├── useModal.ts          # 모달 열기/닫기
├── useToast.ts          # 토스트 알림
├── useDebounce.ts       # 입력 지연
├── useWindowSize.ts     # 창 크기 감지
└── useOutsideClick.ts   # 외부 클릭 감지

features/{feature}/presentation/hooks/
├── useAuth.ts           # 로그인/로그아웃
├── useLoan.ts           # 대출 목록, 신청
├── useGame.ts           # 게임 상태, 캐릭터 이동
└── useLoanDetail.ts     # 대출 상세 (단건)
```

---

## shared 훅 — 작성법

shared 훅은 **어느 feature에서도 가져다 쓸 수 있는 순수 UI 유틸 훅**이다.  
도메인을 모르고, UseCase도 모르고, Store도 건드리지 않는다.

### useModal

```ts
// shared/hooks/useModal.ts
import { useState, useCallback } from 'react';

interface UseModalReturn {
  isOpen: boolean;
  open: () => void;
  close: () => void;
  toggle: () => void;
}

/**
 * 모달의 열림/닫힘 상태를 관리하는 훅
 *
 * 사용 예시:
 *   const { isOpen, open, close } = useModal();
 *   <Button onClick={open}>열기</Button>
 *   <Modal isOpen={isOpen} onClose={close} />
 */
export const useModal = (initialState = false): UseModalReturn => {
  const [isOpen, setIsOpen] = useState(initialState);

  const open = useCallback(() => setIsOpen(true), []);
  const close = useCallback(() => setIsOpen(false), []);
  const toggle = useCallback(() => setIsOpen((prev) => !prev), []);

  return { isOpen, open, close, toggle };
};
```

### useDebounce

```ts
// shared/hooks/useDebounce.ts
import { useState, useEffect } from 'react';

/**
 * 값이 빠르게 바뀔 때, 마지막 변경 이후 delay ms 뒤의 값만 반환
 *
 * 사용 예시: 검색창 타이핑 중엔 API를 안 부르고
 *            멈추고 500ms 뒤에만 호출하고 싶을 때
 *
 *   const debouncedKeyword = useDebounce(keyword, 500);
 *   useEffect(() => { search(debouncedKeyword); }, [debouncedKeyword]);
 */
export const useDebounce = <T>(value: T, delay: number): T => {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedValue(value), delay);
    return () => clearTimeout(timer);   // 다음 변경 전에 이전 타이머 취소
  }, [value, delay]);

  return debouncedValue;
};
```

### useOutsideClick

```ts
// shared/hooks/useOutsideClick.ts
import { useEffect, RefObject } from 'react';

/**
 * 특정 요소 바깥을 클릭했을 때 콜백 실행
 *
 * 사용 예시: 드롭다운 메뉴 바깥 클릭 시 닫기
 *
 *   const ref = useRef<HTMLDivElement>(null);
 *   useOutsideClick(ref, () => setIsOpen(false));
 */
export const useOutsideClick = (
  ref: RefObject<HTMLElement>,
  callback: () => void
): void => {
  useEffect(() => {
    const handleClick = (event: MouseEvent) => {
      if (ref.current && !ref.current.contains(event.target as Node)) {
        callback();
      }
    };
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, [ref, callback]);
};
```

### useWindowSize

```ts
// shared/hooks/useWindowSize.ts
import { useState, useEffect } from 'react';

interface WindowSize {
  width: number;
  height: number;
}

/**
 * 현재 브라우저 창 크기를 반환하고, 변경 시 자동 업데이트
 *
 * 사용 예시: 모바일/데스크탑 레이아웃 분기
 *
 *   const { width } = useWindowSize();
 *   const isMobile = width < 768;
 */
export const useWindowSize = (): WindowSize => {
  const [size, setSize] = useState<WindowSize>({
    width: window.innerWidth,
    height: window.innerHeight,
  });

  useEffect(() => {
    const handleResize = () =>
      setSize({ width: window.innerWidth, height: window.innerHeight });
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  return size;
};
```

---

## feature 훅 — 작성법

feature 훅은 **UseCase 호출 + Store 연결 + 로컬 UI 상태**를 조합하는 계층이다.  
컴포넌트는 "어떻게 동작하는지" 알 필요 없이, 훅이 다 처리하고 결과만 받는다.

### 흐름 이해

```
컴포넌트 (LoginForm)
    ↓ login('email', 'pw') 호출
feature 훅 (useAuth)
    ├── isLoading = true 설정
    ├── container.resolve(LoginUseCase)   ← DI 컨테이너에서 UseCase 꺼냄
    ├── useCase.execute(credentials)       ← 비즈니스 로직 실행
    ├── setUser(result)                    ← Store 업데이트
    └── isLoading = false 설정
컴포넌트
    └── isLoading 보고 버튼 비활성화 렌더링
```

### 기본 구조

```ts
// features/auth/presentation/hooks/useAuth.ts
import { useState, useCallback } from 'react';
import { container } from '@core/di/container';
import { useAuthStore } from '@core/store/authStore';
import { LoginUseCase } from '../../domain/usecases/LoginUseCase';
import { LogoutUseCase } from '../../domain/usecases/LogoutUseCase';

export const useAuth = () => {
  // ① 로컬 UI 상태 (이 훅 안에서만 쓰는 상태)
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // ② 전역 상태 구독 (Store에서 필요한 것만 꺼냄)
  const user = useAuthStore((s) => s.user);
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const setUser = useAuthStore((s) => s.setUser);
  const clearUser = useAuthStore((s) => s.clearUser);

  // ③ 액션 정의 (UseCase 호출 + Store 업데이트)
  const login = useCallback(async (email: string, password: string) => {
    setIsLoading(true);
    setError(null);
    try {
      const useCase = container.resolve(LoginUseCase);   // DI로 UseCase 획득
      const result = await useCase.execute({ email, password });
      setUser(result);                                    // 결과를 Store에 저장
    } catch (e) {
      setError(e instanceof Error ? e.message : '오류가 발생했습니다.');
    } finally {
      setIsLoading(false);
    }
  }, [setUser]);

  const logout = useCallback(async () => {
    const useCase = container.resolve(LogoutUseCase);
    await useCase.execute();
    clearUser();
  }, [clearUser]);

  // ④ 컴포넌트에 필요한 것만 반환
  return { user, isAuthenticated, isLoading, error, login, logout };
};
```

### 데이터 목록 조회 패턴

```ts
// features/loan/presentation/hooks/useLoan.ts
export const useLoan = () => {
  const [loans, setLoans] = useState<Loan[]>([]);
  const [isLoading, setIsLoading] = useState(false);       // 목록 조회 로딩
  const [isSubmitting, setIsSubmitting] = useState(false); // 신청 제출 로딩
  const [error, setError] = useState<string | null>(null);

  // 마운트 시 자동 조회
  useEffect(() => {
    const fetch = async () => {
      setIsLoading(true);
      try {
        const useCase = container.resolve(GetLoanListUseCase);
        setLoans(await useCase.execute());
      } catch {
        setError('대출 목록을 불러오지 못했습니다.');
      } finally {
        setIsLoading(false);
      }
    };
    fetch();
  }, []);

  const applyLoan = useCallback(async (request: LoanRequest) => {
    setIsSubmitting(true);
    setError(null);
    try {
      const useCase = container.resolve(ApplyLoanUseCase);
      const newLoan = await useCase.execute(request);
      setLoans((prev) => [...prev, newLoan]);  // 목록에 추가
      return newLoan;
    } catch (e) {
      setError(e instanceof Error ? e.message : '신청 중 오류가 발생했습니다.');
      throw e;  // 컴포넌트에서도 실패 감지 가능하도록 re-throw
    } finally {
      setIsSubmitting(false);
    }
  }, []);

  return { loans, isLoading, isSubmitting, error, applyLoan };
};
```

---

## 반환값 설계 원칙

```ts
// ✅ 항상 객체로 반환 (이름으로 구조분해, 순서 무관)
return {
  data,
  isLoading,
  isSubmitting,
  error,
  submit,
  refetch,
};

// ✅ 로딩 상태는 역할별로 분리 (UX 개선)
isLoading    // 초기 데이터 조회 중
isSubmitting // 폼 제출 중
isDeleting   // 삭제 중

// ❌ 배열 반환 (단순 on/off 훅 제외)
return [data, isLoading, submit];  // 이름 없이 순서로만 구분 - 혼란 발생
```

---

## 금지 패턴

```ts
// ❌ shared 훅에서 UseCase나 도메인 개념 사용
// shared/hooks/useModal.ts
import { LoginUseCase } from '@features/auth/...';  // 절대 금지

// ❌ feature 훅에서 axios 직접 호출
export const useLoan = () => {
  const apply = async () => {
    await axios.post('/loan');  // UseCase → Repository → DataSource 경유 필수
  };
};

// ❌ 훅 하나에 여러 feature 혼합
export const useAuthAndLoan = () => { ... };

// ❌ 컴포넌트 내부에 로직 인라인 작성 (훅으로 분리해야 함)
const LoanPage = () => {
  useEffect(() => {
    axios.get('/loan').then(...);  // useLoan() 훅으로 분리할 것
  }, []);
};
```
