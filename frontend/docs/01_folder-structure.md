# 📁 폴더 구조 설계 원칙

## 개요

Feature 기반 Clean Architecture를 적용한 폴더 구조.  
각 Feature는 **domain / data / presentation** 3계층으로 분리되며,  
계층 간 의존성은 **안쪽(domain)으로만** 향한다.

---

## 전체 구조

```
src/
├── features/               # 기능별 도메인 분리
│   ├── auth/
│   ├── game/
│   ├── loan/
│   ├── property/
│   ├── card/
│   └── mypage/
│
├── shared/                 # 전체 feature에서 공유하는 순수 공통 자원
│   ├── components/
│   ├── hooks/
│   └── utils/
│
├── core/                   # 인프라 / 기술적 설정 (DI, 네트워크, 스토어, 에러)
│   ├── network/
│   ├── store/
│   ├── di/
│   └── error/
│
├── app/                    # 앱 진입점, 라우팅, 전역 Provider
│   ├── App.tsx
│   ├── Router.tsx
│   └── providers/
│
└── assets/                 # 정적 자원
    ├── sprites/
    ├── sounds/
    └── images/
```

---

## Feature 내부 구조

```
features/{featureName}/
├── domain/                 # 순수 비즈니스 로직 (외부 의존 없음)
│   ├── entities/           # 핵심 도메인 객체 (불변, 순수 TS)
│   ├── repositories/       # Repository 인터페이스 (I 접두사)
│   └── usecases/           # UseCase 클래스
│
├── data/                   # 외부 데이터 처리 (API, LocalStorage 등)
│   ├── models/             # DTO / 응답 모델 (Model 접미사)
│   ├── datasources/        # 실제 데이터 소스 구현체
│   └── repositories/       # IRepository 구현체 (Impl 접미사)
│
└── presentation/           # UI 계층
    ├── pages/              # 라우트에 연결되는 페이지 컴포넌트
    ├── components/         # 해당 feature 전용 컴포넌트
    └── hooks/              # 해당 feature 전용 커스텀 훅
```

---

## 계층별 역할 및 의존 방향

```
presentation  →  domain  ←  data
                  ↑
           (의존성 역전)
```

| 계층 | 역할 | 의존 가능한 계층 |
|------|------|----------------|
| `domain` | 핵심 비즈니스 규칙 | 없음 (순수) |
| `data` | 외부 데이터 연동 | domain (인터페이스만) |
| `presentation` | UI 렌더링, 사용자 인터랙션 | domain (usecase, entity) |

---

## Feature 간 참조 규칙

```
✅ 허용
features/loan/presentation/hooks/useLoan.ts
  → shared/utils/formatter.ts          # shared는 항상 참조 가능
  → core/network/apiClient.ts          # core는 항상 참조 가능

❌ 금지
features/loan/  →  features/auth/      # feature 간 직접 참조 금지
features/game/  →  features/mypage/    # feature 간 직접 참조 금지
```

> Feature 간 데이터 공유가 필요하면 `shared/` 또는 `core/store/`를 통해 간접 공유.

---

## shared vs core 구분 기준

| 구분 | 위치 | 기준 |
|------|------|------|
| UI 컴포넌트, 유틸, 공통 훅 | `shared/` | 여러 feature의 **UI/로직**에서 재사용 |
| DI 컨테이너, API 클라이언트, 전역 상태, 에러 | `core/` | 앱의 **기술적 인프라** |

---

## 컴포넌트 폴더 구조 (복잡도에 따라 선택)

### 단순 컴포넌트 (파일 1~2개)
```
components/
└── Button.tsx
```

### 복잡 컴포넌트 (스타일, 서브컴포넌트 존재)
```
components/
└── Character/
    ├── Character.tsx
    ├── Character.module.css
    ├── CharacterSprite.tsx       # 서브 컴포넌트
    └── index.ts                  # 외부 공개 인터페이스
```

---

## index.ts (Barrel Export) 규칙

- 각 폴더의 공개 인터페이스는 `index.ts`로 통일
- feature 외부에서는 반드시 `index.ts`를 통해서만 import

```ts
// features/auth/index.ts
export { LoginPage } from './presentation/pages/LoginPage';
export { useAuth } from './presentation/hooks/useAuth';
export type { User } from './domain/entities/User';
```

```ts
// ✅ 올바른 import
import { useAuth } from '@/features/auth';

// ❌ 잘못된 import (내부 구조 직접 참조)
import { useAuth } from '@/features/auth/presentation/hooks/useAuth';
```

---

## Path Alias 설정

```json
// tsconfig.json
{
  "compilerOptions": {
    "paths": {
      "@/*": ["src/*"],
      "@features/*": ["src/features/*"],
      "@shared/*": ["src/shared/*"],
      "@core/*": ["src/core/*"],
      "@assets/*": ["src/assets/*"]
    }
  }
}
```
