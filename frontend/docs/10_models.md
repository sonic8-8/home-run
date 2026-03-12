# 📦 Model (DTO) 설계 원칙

## 개요

Model은 **외부 데이터(API 응답/요청, LocalStorage)의 형태**를 표현한다.  
Entity가 "비즈니스가 보는 세계"라면, Model은 "외부 시스템이 보내는 세계"다.  
변환(Mapping)은 Repository 구현체의 책임이다.

---

## 위치

```
features/{feature}/data/models/
├── UserModel.ts           # 응답 모델
├── LoanModel.ts
├── LoanRequestModel.ts    # 요청 모델
└── CharacterModel.ts
```

---

## Model 작성 규칙

### 1. Model 접미사 필수
### 2. API 응답 형태 그대로 (snake_case 허용)
### 3. 순수 타입만 (메서드, 로직 없음)
### 4. optional 필드는 명시적으로 `?` 처리

```ts
// features/auth/data/models/UserModel.ts

// ✅ 응답 모델 (API → 앱)
export interface UserModel {
  user_id: string;
  user_name: string;
  email_address: string;
  credit_score: number;
  created_at: string;       // API는 보통 string으로 날짜 반환
  updated_at: string;
  profile_image_url?: string;  // optional 필드
}

// ✅ 요청 모델 (앱 → API)
export interface LoginRequestModel {
  email: string;
  password: string;
}

export interface LoginResponseModel {
  access_token: string;
  refresh_token: string;
  expires_in: number;
  user: UserModel;
}
```

---

## 응답/요청 모델 분리 기준

| 모델 종류 | 접미사 패턴 | 방향 | 예시 |
|----------|-----------|------|------|
| API 응답 | `{Name}Model` | 서버 → 앱 | `UserModel`, `LoanModel` |
| API 요청 Body | `{Name}RequestModel` | 앱 → 서버 | `LoginRequestModel` |
| API 응답 래퍼 | `{Name}ResponseModel` | 서버 → 앱 | `LoginResponseModel` |
| 로컬 저장 | `{Name}LocalModel` | 앱 ↔ 로컬 | `GameStateLocalModel` |

---

## 전체 예시

```ts
// features/loan/data/models/LoanModel.ts

// 서버 응답
export interface LoanModel {
  loan_id: string;
  user_id: string;
  loan_amount: number;
  loan_term: number;         // 개월 수
  interest_rate: number;
  status: string;            // 'PENDING' | 'APPROVED' | 'REJECTED'
  applied_at: string;
  approved_at?: string;
  rejected_reason?: string;
}

// 대출 신청 요청
export interface LoanApplyRequestModel {
  amount: number;
  term: number;
  purpose: string;
}

// 목록 응답
export interface LoanListResponseModel {
  loans: LoanModel[];
  total_count: number;
  page: number;
  page_size: number;
}
```

```ts
// features/game/data/models/CharacterModel.ts
export interface CharacterModel {
  id: string;
  name: string;
  pos_x: number;
  pos_y: number;
  state: string;
  sprite_key: string;
  last_saved_at: string;
}

export interface GameStateLocalModel {
  character: CharacterModel;
  current_scene_id: string;
  play_time_seconds: number;
}
```

---

## Model → Entity 변환 (Repository 책임)

변환 로직은 `RepositoryImpl`의 `private toEntity()` 메서드에 위치한다.

```ts
// ✅ Repository에서 변환
private toLoanEntity(model: LoanModel): Loan {
  return {
    id: model.loan_id,
    userId: model.user_id,
    amount: model.loan_amount,
    term: model.loan_term,
    interestRate: model.interest_rate,
    status: model.status as LoanStatus,
    appliedAt: new Date(model.applied_at),
    approvedAt: model.approved_at ? new Date(model.approved_at) : undefined,
    rejectedReason: model.rejected_reason,
  };
}
```

---

## API 응답 공통 래퍼

서버가 공통 응답 형식을 사용하는 경우:

```ts
// core/network/ApiResponseModel.ts
export interface ApiSuccessResponse<T> {
  success: true;
  data: T;
  message?: string;
}

export interface ApiErrorResponse {
  success: false;
  error: {
    code: string;
    message: string;
    details?: unknown;
  };
}

export type ApiResponse<T> = ApiSuccessResponse<T> | ApiErrorResponse;

// 사용 예시 (DataSource에서)
const response = await apiClient.post<ApiResponse<LoginResponseModel>>('/auth/login', body);
if (!response.data.success) {
  throw new NetworkError(response.data.error.message, response.status);
}
return response.data.data;
```

---

## 금지 패턴

```ts
// ❌ Model을 Entity 대신 UseCase 반환 타입으로 사용
async execute(): Promise<UserModel> { ... }    // Entity 반환해야 함

// ❌ Model에 메서드 포함
export class LoanModel {
  toLoan(): Loan { ... }    // 변환은 Repository 책임
}

// ❌ Entity에서 Model import
// domain/entities/User.ts
import { UserModel } from '../../data/models/UserModel';   // 계층 역전 위반

// ❌ 컴포넌트에서 Model 직접 사용
const LoanCard: React.FC<{ loan: LoanModel }> = ...   // Entity 타입 사용해야 함
```
