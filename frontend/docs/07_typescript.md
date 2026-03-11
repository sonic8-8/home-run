# 🔷 TypeScript 설계 원칙

## 개요

TypeScript를 통해 **컴파일 타임 안전성**을 확보하고  
코드의 의도를 타입으로 문서화한다.

---

## 기본 규칙

### any 사용 금지
```ts
// ❌ any 사용 - 타입 안전성 포기
function processData(data: any): any { ... }

// ✅ 명시적 타입 또는 제네릭 사용
function processData<T>(data: T): ProcessedResult<T> { ... }

// ✅ 정말 모를 때는 unknown + 타입 가드
function parseResponse(data: unknown): User {
  if (!isUser(data)) throw new Error('Invalid user data');
  return data;
}
```

### 타입 추론 활용 (명시 vs 추론 균형)
```ts
// ✅ 추론 가능하면 생략
const name = 'Alice';                    // string 추론
const users: User[] = [];               // 초기화 시 타입 명시
const count = users.length;             // number 추론

// ✅ 함수 반환 타입은 명시 (public API)
async function login(credentials: LoginCredentials): Promise<User> { ... }

// ✅ 복잡한 객체는 타입 명시
const config: AppConfig = {
  apiUrl: process.env.VITE_API_URL ?? '',
  timeout: 5000,
};
```

---

## 타입 vs 인터페이스 선택 기준

```ts
// interface: 객체 형태 정의, 확장 가능성 있을 때
interface User {
  id: string;
  name: string;
  email: string;
}

interface AdminUser extends User {
  role: 'admin';
  permissions: string[];
}

// type: 유니온, 교차, 튜플, 유틸리티 타입 조합
type Direction = 'up' | 'down' | 'left' | 'right';
type CharacterState = 'idle' | 'walking' | 'running' | 'stunned';
type LoanStatus = 'pending' | 'approved' | 'rejected';

type ApiResponse<T> = {
  data: T;
  status: number;
  message: string;
};

type PartialLoan = Partial<Loan>;
type ReadonlyUser = Readonly<User>;
```

---

## Domain Entity 타입 설계

```ts
// features/auth/domain/entities/User.ts

// ✅ 불변 Entity는 Readonly 적용
export interface User {
  readonly id: string;
  readonly name: string;
  readonly email: string;
  readonly creditScore: number;
  readonly createdAt: Date;
}

// ✅ 값 객체는 브랜드 타입으로 구분
export type UserId = string & { readonly _brand: 'UserId' };
export type LoanId = string & { readonly _brand: 'LoanId' };

// 혼동 방지 예시
function getUser(id: UserId): Promise<User> { ... }
function getLoan(id: LoanId): Promise<Loan> { ... }

// getUser(loanId) ← 컴파일 에러 발생 (브랜드 타입 덕분에)
```

---

## 에러 타입 설계

```ts
// core/error/AppError.ts
export class AppError extends Error {
  constructor(
    message: string,
    public readonly code: string,
    public readonly statusCode?: number
  ) {
    super(message);
    this.name = 'AppError';
  }
}

export class DomainError extends AppError {
  constructor(message: string) {
    super(message, 'DOMAIN_ERROR');
    this.name = 'DomainError';
  }
}

export class NetworkError extends AppError {
  constructor(message: string, statusCode: number) {
    super(message, 'NETWORK_ERROR', statusCode);
    this.name = 'NetworkError';
  }
}

// 타입 가드
export function isDomainError(error: unknown): error is DomainError {
  return error instanceof DomainError;
}
```

---

## 제네릭 활용 패턴

```ts
// API 응답 래퍼
interface ApiResponse<T> {
  data: T;
  status: 'success' | 'error';
  message?: string;
}

// 페이지네이션
interface PaginatedResponse<T> {
  items: T[];
  total: number;
  page: number;
  limit: number;
}

// 사용 예시
async function fetchLoans(): Promise<PaginatedResponse<Loan>> { ... }
async function fetchUser(): Promise<ApiResponse<User>> { ... }

// Repository 제네릭 기반 추상화
interface IBaseRepository<T, ID = string> {
  findById(id: ID): Promise<T | null>;
  findAll(): Promise<T[]>;
  save(entity: T): Promise<T>;
  delete(id: ID): Promise<void>;
}

interface ILoanRepository extends IBaseRepository<Loan> {
  applyLoan(request: LoanRequest): Promise<Loan>;
  getLoansByUser(userId: string): Promise<Loan[]>;
}
```

---

## 유틸리티 타입 활용

```ts
// Partial: 일부 필드만 업데이트
function updateCharacter(id: string, updates: Partial<Character>): Promise<Character>

// Required: 모든 필드 필수화
type StrictLoanRequest = Required<LoanRequest>;

// Pick: 필요한 필드만 추출
type LoginCredentials = Pick<User, 'email'> & { password: string };

// Omit: 특정 필드 제외
type CreateUserInput = Omit<User, 'id' | 'createdAt'>;

// Record: 키-값 맵핑
type SceneMap = Record<string, Scene>;
type CharacterAnimations = Record<CharacterState, string[]>;

// Discriminated Union: 상태별 타입 안전성
type AuthState =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'authenticated'; user: User }
  | { status: 'error'; error: string };

// switch문에서 완전성 검사
function renderAuthState(state: AuthState): React.ReactNode {
  switch (state.status) {
    case 'idle': return <IdleView />;
    case 'loading': return <Spinner />;
    case 'authenticated': return <UserProfile user={state.user} />;
    case 'error': return <ErrorView message={state.error} />;
    // default가 없어도 모든 케이스 처리됨 (타입 안전)
  }
}
```

---

## 타입 가드 패턴

```ts
// 사용자 정의 타입 가드
function isUser(value: unknown): value is User {
  return (
    typeof value === 'object' &&
    value !== null &&
    'id' in value &&
    'email' in value
  );
}

// in 연산자 활용
function isAdminUser(user: User | AdminUser): user is AdminUser {
  return 'role' in user && user.role === 'admin';
}

// 에러 타입 가드
function handleError(error: unknown): string {
  if (error instanceof DomainError) return error.message;
  if (error instanceof NetworkError) return `네트워크 오류: ${error.statusCode}`;
  if (error instanceof Error) return error.message;
  return '알 수 없는 오류가 발생했습니다.';
}
```

---

## Enum vs Union Type

```ts
// ✅ Union Type 권장 (tree-shaking, 직렬화 용이)
type LoanStatus = 'PENDING' | 'APPROVED' | 'REJECTED';
type Direction = 'up' | 'down' | 'left' | 'right';

// ✅ const object 패턴 (enum 대체)
export const LOAN_STATUS = {
  Pending: 'PENDING',
  Approved: 'APPROVED',
  Rejected: 'REJECTED',
} as const;

export type LoanStatus = typeof LOAN_STATUS[keyof typeof LOAN_STATUS];

// ⚠️ enum은 비트 플래그나 외부 라이브러리 요구 시에만 사용
```

---

## tsconfig.json 권장 설정

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "ESNext",
    "moduleResolution": "bundler",
    "strict": true,
    "noUncheckedIndexedAccess": true,
    "exactOptionalPropertyTypes": true,
    "noImplicitReturns": true,
    "noFallthroughCasesInSwitch": true,
    "experimentalDecorators": true,
    "emitDecoratorMetadata": true,
    "jsx": "react-jsx",
    "paths": {
      "@/*": ["src/*"],
      "@features/*": ["src/features/*"],
      "@shared/*": ["src/shared/*"],
      "@core/*": ["src/core/*"]
    }
  }
}
```

---

## 금지 패턴

```ts
// ❌ any 타입
const data: any = fetchData();

// ❌ 타입 단언 남용
const user = data as User;                    // 검증 없는 단언

// ✅ 타입 가드로 대체
const user = isUser(data) ? data : null;

// ❌ @ts-ignore 사용
// @ts-ignore
const result = unsafeOperation();

// ✅ @ts-expect-error (이유 명시) 또는 근본 해결

// ❌ Object 타입
function process(obj: Object): void { ... }   // {} 또는 구체 타입 사용
```
