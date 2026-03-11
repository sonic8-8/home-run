# 🗺️ Router 설계 원칙

## 개요

라우팅은 `app/Router.tsx`에서 **중앙 집중 관리**한다.  
각 Route는 해당 Feature의 Page 컴포넌트와 1:1 대응되며,  
인증/권한에 따른 접근 제어는 **Guard 컴포넌트**로 분리한다.

---

## 위치

```
app/
├── App.tsx
├── Router.tsx              # 라우트 정의 (중앙)
└── providers/
    ├── AppProviders.tsx
    └── QueryProvider.tsx

shared/components/
├── PrivateRoute.tsx        # 인증 가드
└── PublicRoute.tsx         # 비인증 전용 (로그인 페이지 등)
```

---

## 라우트 경로 상수

경로는 **상수로 중앙 관리**한다. 하드코딩 금지.

```ts
// app/routes.ts
export const ROUTES = {
  // 인증
  LOGIN: '/login',
  REGISTER: '/register',

  // 메인
  HOME: '/',

  // 게임
  GAME: '/game',

  // 대출
  LOAN: '/loan',
  LOAN_DETAIL: (id: string) => `/loan/${id}`,

  // 부동산
  PROPERTY: '/property',
  PROPERTY_DETAIL: (id: string) => `/property/${id}`,

  // 카드
  CARD: '/card',

  // 마이페이지
  MY_PAGE: '/mypage',

  // 에러
  NOT_FOUND: '*',
} as const;

// 사용 예시
navigate(ROUTES.LOAN_DETAIL('abc-123'));
<Link to={ROUTES.LOAN}>대출</Link>
```

---

## Router.tsx 구조

```tsx
// app/Router.tsx
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { ROUTES } from './routes';

// Lazy import (코드 스플리팅)
import { lazy, Suspense } from 'react';

const LoginPage = lazy(() => import('@features/auth/presentation/pages/LoginPage'));
const GamePage = lazy(() => import('@features/game/presentation/pages/GamePage'));
const LoanPage = lazy(() => import('@features/loan/presentation/pages/LoanPage'));
const LoanDetailPage = lazy(() => import('@features/loan/presentation/pages/LoanDetailPage'));
const PropertyPage = lazy(() => import('@features/property/presentation/pages/PropertyPage'));
const CardPage = lazy(() => import('@features/card/presentation/pages/CardPage'));
const MyPage = lazy(() => import('@features/mypage/presentation/pages/MyPage'));
const NotFoundPage = lazy(() => import('@shared/components/NotFoundPage'));

import { PrivateRoute } from '@shared/components/PrivateRoute';
import { PublicRoute } from '@shared/components/PublicRoute';
import { RootLayout } from '@shared/components/RootLayout';
import { PageSpinner } from '@shared/components/PageSpinner';

const router = createBrowserRouter([
  {
    // 인증 불필요 라우트
    element: <PublicRoute />,
    children: [
      { path: ROUTES.LOGIN, element: <LoginPage /> },
    ],
  },
  {
    // 인증 필요 라우트
    element: <PrivateRoute />,
    children: [
      {
        element: <RootLayout />,   // 공통 레이아웃 (네비게이션 등)
        children: [
          { path: ROUTES.HOME, element: <GamePage /> },
          { path: ROUTES.GAME, element: <GamePage /> },
          { path: ROUTES.LOAN, element: <LoanPage /> },
          { path: '/loan/:id', element: <LoanDetailPage /> },
          { path: ROUTES.PROPERTY, element: <PropertyPage /> },
          { path: ROUTES.CARD, element: <CardPage /> },
          { path: ROUTES.MY_PAGE, element: <MyPage /> },
        ],
      },
    ],
  },
  { path: ROUTES.NOT_FOUND, element: <NotFoundPage /> },
]);

export const Router = () => (
  <Suspense fallback={<PageSpinner />}>
    <RouterProvider router={router} />
  </Suspense>
);
```

---

## Guard 컴포넌트

### PrivateRoute - 인증 필수
```tsx
// shared/components/PrivateRoute.tsx
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '@core/store/authStore';
import { ROUTES } from '@app/routes';

export const PrivateRoute = () => {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const location = useLocation();

  if (!isAuthenticated) {
    // 로그인 후 원래 페이지로 돌아올 수 있도록 state에 from 저장
    return <Navigate to={ROUTES.LOGIN} state={{ from: location }} replace />;
  }

  return <Outlet />;
};
```

### PublicRoute - 비인증 전용
```tsx
// shared/components/PublicRoute.tsx
import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@core/store/authStore';
import { ROUTES } from '@app/routes';

export const PublicRoute = () => {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);

  // 이미 로그인된 경우 홈으로 리다이렉트
  if (isAuthenticated) {
    return <Navigate to={ROUTES.HOME} replace />;
  }

  return <Outlet />;
};
```

---

## 로그인 후 원래 페이지 복귀

```tsx
// features/auth/presentation/hooks/useAuth.ts
import { useNavigate, useLocation } from 'react-router-dom';
import { ROUTES } from '@app/routes';

export const useAuth = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const login = async (email: string, password: string) => {
    try {
      const useCase = container.resolve(LoginUseCase);
      const user = await useCase.execute({ email, password });
      setUser(user);

      // PrivateRoute에서 저장해둔 원래 경로로 이동
      const from = (location.state as { from?: Location })?.from?.pathname ?? ROUTES.HOME;
      navigate(from, { replace: true });
    } catch (e) { ... }
  };
};
```

---

## 동적 라우트 (파라미터)

```tsx
// features/loan/presentation/pages/LoanDetailPage.tsx
import { useParams, useNavigate } from 'react-router-dom';
import { ROUTES } from '@app/routes';

export const LoanDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { loan, isLoading } = useLoanDetail(id!);

  if (!loan && !isLoading) {
    // 존재하지 않는 대출 → 목록으로
    navigate(ROUTES.LOAN, { replace: true });
    return null;
  }

  return <div>...</div>;
};
```

---

## Lazy Loading + Code Splitting 원칙

```ts
// ✅ 모든 Page는 lazy import (초기 번들 사이즈 축소)
const LoanPage = lazy(() => import('@features/loan/presentation/pages/LoanPage'));

// ✅ Suspense는 Router 레벨에서 한 번만 적용
<Suspense fallback={<PageSpinner />}>
  <RouterProvider router={router} />
</Suspense>

// ❌ 개별 컴포넌트마다 Suspense 중복 적용 금지 (Router 레벨로 통일)
```

---

## 네비게이션 사용 규칙

```ts
// ✅ 훅/컴포넌트 내부 - useNavigate
const navigate = useNavigate();
navigate(ROUTES.LOAN);
navigate(ROUTES.LOAN_DETAIL(loan.id));
navigate(-1);   // 뒤로가기

// ✅ JSX 내부 링크 - Link 컴포넌트
<Link to={ROUTES.LOAN}>대출 목록</Link>
<NavLink to={ROUTES.GAME}>게임</NavLink>

// ❌ 경로 하드코딩 금지
navigate('/loan/apply');     // ROUTES 상수 사용
<Link to="/mypage">          // ROUTES 상수 사용
```

---

## App.tsx 구조

```tsx
// app/App.tsx
import { Router } from './Router';
import { AppProviders } from './providers/AppProviders';

const App = () => (
  <AppProviders>
    <Router />
  </AppProviders>
);

export default App;
```

```tsx
// app/providers/AppProviders.tsx
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: 1, staleTime: 1000 * 60 * 5 },
  },
});

export const AppProviders: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <QueryClientProvider client={queryClient}>
    {children}
  </QueryClientProvider>
);
```

---

## 금지 패턴

```ts
// ❌ 경로 하드코딩
navigate('/loan/123');
<Link to="/game">

// ❌ 라우트 분산 정의 (각 feature 내부에 라우트 정의)
// features/loan/routes.tsx  ← 금지, app/Router.tsx에서 중앙 관리

// ❌ Page 컴포넌트에서 직접 redirect 로직 (Guard 역할 침범)
export const LoanPage = () => {
  if (!isAuthenticated) return <Navigate to="/login" />;  // PrivateRoute 역할
};
```
