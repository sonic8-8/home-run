# 🏷️ 네이밍 컨벤션 설계 원칙

## 개요

일관된 네이밍은 코드 탐색 비용을 줄이고 계층과 역할을 즉시 파악하게 한다.  
본 문서는 파일명, 클래스명, 변수명, 타입명에 대한 전체 규칙을 정의한다.

---

## 기본 케이스 규칙

| 대상 | 케이스 | 예시 |
|------|--------|------|
| 컴포넌트 파일 | PascalCase | `LoginForm.tsx` |
| 컴포넌트 폴더 | PascalCase | `Character/` |
| 훅 파일 | camelCase, `use` 접두사 | `useAuth.ts` |
| 유틸 파일 | camelCase | `formatter.ts` |
| 타입/인터페이스 파일 | PascalCase | `User.ts`, `IAuthRepository.ts` |
| UseCase 파일 | PascalCase, `UseCase` 접미사 | `LoginUseCase.ts` |
| Repository 파일 | PascalCase | `IAuthRepository.ts`, `AuthRepositoryImpl.ts` |
| DataSource 파일 | PascalCase, `DataSource` 접미사 | `AuthRemoteDataSource.ts` |
| Store 파일 | camelCase, `Store` 접미사 | `gameStore.ts` |
| 상수 파일 | camelCase | `apiConstants.ts` |

---

## Domain 계층 네이밍

### Entity
- **순수 TypeScript 클래스 또는 인터페이스**
- 접두사/접미사 없이 도메인 개념 그대로 명명

```ts
// ✅ 올바른 예
export interface User {
  id: string;
  name: string;
  email: string;
}

export interface Character {
  id: string;
  position: Position;
  state: CharacterState;
}

// ❌ 잘못된 예
export interface UserEntity { ... }   // Entity 접미사 불필요
export interface UserDTO { ... }      // DTO는 data 계층
```

### Repository Interface
- **`I` 접두사 필수** (Interface 명시)
- 동사형 메서드명

```ts
// ✅ 올바른 예
export interface IAuthRepository {
  login(credentials: LoginCredentials): Promise<User>;
  logout(): Promise<void>;
  getMe(): Promise<User>;
}

export interface ILoanRepository {
  applyLoan(request: LoanRequest): Promise<Loan>;
  getLoanStatus(loanId: string): Promise<LoanStatus>;
}
```

### UseCase
- **`UseCase` 접미사 필수**
- 단일 책임: 하나의 UseCase = 하나의 `execute()` 메서드
- 클래스명은 동사+명사 형태

```ts
// ✅ 올바른 예
export class LoginUseCase { ... }
export class ApplyLoanUseCase { ... }
export class MoveCharacterUseCase { ... }
export class GetLoanStatusUseCase { ... }

// ❌ 잘못된 예
export class AuthUseCase { ... }      // 너무 광범위
export class LoanService { ... }      // Service는 data 계층 용어
```

---

## Data 계층 네이밍

### Model (DTO)
- **`Model` 접미사 필수**
- API 응답/요청 형태를 반영

```ts
// ✅ 올바른 예
export interface UserModel {
  user_id: string;       // snake_case도 허용 (API 응답 그대로)
  user_name: string;
  email_address: string;
}

export interface LoanModel {
  loan_id: string;
  amount: number;
  status: string;
}
```

### DataSource
- **`RemoteDataSource`** : API 통신
- **`LocalDataSource`** : LocalStorage, IndexedDB 등

```ts
// ✅ 올바른 예
export class AuthRemoteDataSource { ... }
export class GameLocalDataSource { ... }
export class LoanRemoteDataSource { ... }
```

### Repository 구현체
- **`Impl` 접미사 필수**

```ts
// ✅ 올바른 예
export class AuthRepositoryImpl implements IAuthRepository { ... }
export class LoanRepositoryImpl implements ILoanRepository { ... }
```

---

## Presentation 계층 네이밍

### Page 컴포넌트
- **`Page` 접미사 필수**
- 라우트에 1:1 대응

```ts
// ✅ 올바른 예
export const LoginPage: React.FC = () => { ... }
export const LoanPage: React.FC = () => { ... }
export const GamePage: React.FC = () => { ... }
```

### 일반 컴포넌트
- 접두사/접미사 없이 역할 명시
- UI 역할이 명확히 드러나도록

```tsx
// ✅ 올바른 예
export const LoginForm: React.FC = () => { ... }
export const LoanCard: React.FC<LoanCardProps> = () => { ... }
export const CharacterSprite: React.FC<CharacterSpriteProps> = () => { ... }

// ❌ 잘못된 예
export const LoginComponent: React.FC = () => { ... }   // Component 접미사 불필요
export const Loan: React.FC = () => { ... }              // 너무 모호
```

### 커스텀 훅
- **`use` 접두사 필수**
- feature 전용은 feature 이름 포함 권장

```ts
// ✅ 올바른 예
export const useAuth = () => { ... }
export const useLoan = () => { ... }
export const useGame = () => { ... }
export const useModal = () => { ... }       // shared
```

---

## 타입 / 인터페이스 네이밍

```ts
// Props 타입: 컴포넌트명 + Props
interface LoginFormProps { ... }
interface LoanCardProps { ... }

// 상태 타입: 명사 그대로
interface GameState { ... }
interface AuthState { ... }

// 이벤트 핸들러: on + 동사
onLogin: () => void;
onSubmit: (data: FormData) => void;
onCharacterMove: (direction: Direction) => void;

// Boolean 변수/상태: is/has/can 접두사
isLoading: boolean;
isAuthenticated: boolean;
hasError: boolean;
canApplyLoan: boolean;
```

---

## 상수 네이밍

```ts
// 상수: UPPER_SNAKE_CASE
export const API_BASE_URL = 'https://api.example.com';
export const MAX_LOAN_AMOUNT = 100_000_000;
export const GAME_TICK_RATE = 60;

// enum: PascalCase (값은 PascalCase)
export enum LoanStatus {
  Pending = 'PENDING',
  Approved = 'APPROVED',
  Rejected = 'REJECTED',
}

export enum CharacterState {
  Idle = 'IDLE',
  Walking = 'WALKING',
  Running = 'RUNNING',
}
```

---

## 금지 패턴

```ts
// ❌ 의미 없는 접두사/접미사
IUser (인터페이스가 아닌데 I 붙이기)
UserClass
LoginHelper
AuthManager (UseCase로 대체)

// ❌ 축약어 남용
usrNm    →  userName
btnClk   →  onButtonClick
idx      →  index (루프 외부 사용 시)

// ❌ 계층 혼용 네이밍
LoginService (presentation에서 Service 사용)
UserRepository (domain entity를 Repository라 부름)
```
