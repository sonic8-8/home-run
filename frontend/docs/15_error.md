# 🚨 core/error 설계 원칙

## 에러를 왜 분류해야 하나?

```
에러가 발생했을 때 앱이 해야 할 일은 상황마다 다르다

DomainError  →  "대출 금액은 0보다 커야 합니다"
               → 사용자에게 폼 에러 메시지로 표시

NetworkError →  "서버 오류가 발생했습니다"
               → Toast 알림으로 표시

UnauthorizedError → 토큰 만료
               → 로그인 페이지로 이동

에러 타입이 없으면?
  → catch (e) { console.log(e) } 하고 뭘 해야 할지 모름
  → 모든 에러를 동일하게 처리하거나 무시하게 됨
```

---

## 위치

```
core/error/
└── AppError.ts    # 모든 커스텀 에러 클래스 + 타입 가드
```

---

## AppError.ts

```ts
// core/error/AppError.ts

// ── 기반 클래스 ────────────────────────────────────────────────────────
/**
 * 모든 앱 커스텀 에러의 기반 클래스
 * 표준 Error에 code 필드를 추가
 */
export class AppError extends Error {
  constructor(
    message: string,
    public readonly code: string
  ) {
    super(message);
    this.name = 'AppError';

    // TypeScript에서 Error 상속 시 필요한 처리
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

// ── 도메인 에러 ────────────────────────────────────────────────────────
/**
 * 비즈니스 규칙 위반 에러
 * UseCase에서 발생시키며, 사용자에게 직접 보여줄 수 있는 메시지를 담는다
 *
 * 예: "대출 금액은 1만원 이상이어야 합니다"
 *     "신용 점수가 부족합니다"
 *     "이미 진행 중인 대출이 있습니다"
 */
export class DomainError extends AppError {
  constructor(message: string) {
    super(message, 'DOMAIN_ERROR');
    this.name = 'DomainError';
  }
}

// ── 네트워크 에러 ──────────────────────────────────────────────────────
/**
 * API 통신 관련 에러
 * interceptors.ts에서 발생시키며, HTTP 상태 코드를 포함
 *
 * 예: 500 서버 오류, 네트워크 연결 없음
 */
export class NetworkError extends AppError {
  constructor(
    message: string,
    public readonly statusCode: number
  ) {
    super(message, 'NETWORK_ERROR');
    this.name = 'NetworkError';
  }
}

// ── 인증 에러 ──────────────────────────────────────────────────────────
/**
 * 인증/인가 실패 에러
 * 토큰 만료, 권한 없음 등
 */
export class UnauthorizedError extends AppError {
  constructor(message = '인증이 필요합니다.') {
    super(message, 'UNAUTHORIZED');
    this.name = 'UnauthorizedError';
  }
}

export class ForbiddenError extends AppError {
  constructor(message = '접근 권한이 없습니다.') {
    super(message, 'FORBIDDEN');
    this.name = 'ForbiddenError';
  }
}

// ── 유효성 검사 에러 ───────────────────────────────────────────────────
/**
 * 입력값 유효성 검사 실패 에러
 * 여러 필드 에러를 한번에 담을 수 있음
 */
export class ValidationError extends AppError {
  constructor(
    message: string,
    public readonly fields?: Record<string, string>
  ) {
    super(message, 'VALIDATION_ERROR');
    this.name = 'ValidationError';
  }
}

// ── 타입 가드 ─────────────────────────────────────────────────────────
// "이 에러가 어떤 종류인지" 확인하는 함수들
// if (isDomainError(e)) 형태로 사용

export function isDomainError(error: unknown): error is DomainError {
  return error instanceof DomainError;
}

export function isNetworkError(error: unknown): error is NetworkError {
  return error instanceof NetworkError;
}

export function isUnauthorizedError(error: unknown): error is UnauthorizedError {
  return error instanceof UnauthorizedError;
}

export function isValidationError(error: unknown): error is ValidationError {
  return error instanceof ValidationError;
}

// ── 에러 메시지 변환 유틸 ─────────────────────────────────────────────
/**
 * 어떤 에러든 사용자에게 보여줄 메시지 문자열로 변환
 * catch (e) 블록에서 e의 타입을 모를 때 사용
 */
export function toErrorMessage(error: unknown): string {
  if (isDomainError(error)) return error.message;       // 도메인 에러: 그대로 표시
  if (isNetworkError(error)) return error.message;      // 네트워크 에러: 그대로 표시
  if (error instanceof Error) return error.message;     // 일반 에러: message 사용
  return '알 수 없는 오류가 발생했습니다.';               // 나머지: 일반 메시지
}
```

---

## 에러 계층 구조

```
Error (JavaScript 기본)
  └── AppError                   (code 추가)
        ├── DomainError          → UseCase에서 발생, 사용자에게 표시 가능
        ├── NetworkError         → interceptors에서 발생, statusCode 포함
        ├── UnauthorizedError    → 401: 토큰 만료/미인증
        ├── ForbiddenError       → 403: 권한 없음
        └── ValidationError      → 유효성 검사 실패, fields 포함
```

---

## 각 계층에서의 에러 처리

### UseCase — DomainError 발생

```ts
// features/loan/domain/usecases/ApplyLoanUseCase.ts
import { DomainError } from '@core/error/AppError';

@injectable()
export class ApplyLoanUseCase {
  async execute(input: ApplyLoanInput): Promise<Loan> {
    // 비즈니스 규칙 위반 → DomainError
    if (input.amount < 10_000) {
      throw new DomainError('대출 금액은 최소 1만원 이상이어야 합니다.');
    }
    if (input.amount > 100_000_000) {
      throw new DomainError('대출 금액은 최대 1억원까지 가능합니다.');
    }
    if (input.term < 6 || input.term > 60) {
      throw new DomainError('대출 기간은 6개월 ~ 60개월 사이여야 합니다.');
    }

    return this.loanRepository.applyLoan(input);
  }
}
```

### interceptors.ts — NetworkError 발생

```ts
// 이미 13_core-network.md에서 다뤘지만 에러 관점으로 보면
if (status >= 500) {
  throw new NetworkError('서버 오류가 발생했습니다.', status);
}
if (!error.response) {
  throw new NetworkError('인터넷 연결을 확인해주세요.', 0);
}
```

### feature 훅 — 에러 타입별 분기 처리

```ts
// features/loan/presentation/hooks/useLoan.ts
import { isDomainError, isNetworkError, toErrorMessage } from '@core/error/AppError';

const applyLoan = async (request: LoanRequest) => {
  try {
    const useCase = container.resolve(ApplyLoanUseCase);
    return await useCase.execute(request);
  } catch (e) {
    if (isDomainError(e)) {
      // 비즈니스 규칙 위반: 폼 에러로 표시
      setError(e.message);
    } else if (isNetworkError(e)) {
      // 서버/네트워크 오류: Toast로 표시
      showToast(e.message, 'error');
    } else {
      // 예상 못한 에러: 일반 메시지
      setError('대출 신청 중 오류가 발생했습니다.');
    }
  }
};
```

### 공통 에러 메시지 변환 (타입 모를 때)

```ts
// 타입을 정확히 알 수 없는 상황
} catch (e) {
  setError(toErrorMessage(e));  // 어떤 에러든 안전하게 문자열로 변환
}
```

---

## 에러 처리 위치 정리

| 에러 종류 | 발생 위치 | 처리 위치 | 표시 방법 |
|----------|----------|----------|----------|
| DomainError | UseCase | feature 훅 | 폼 에러 메시지 |
| NetworkError | interceptors | feature 훅 / 전역 | Toast 알림 |
| UnauthorizedError | interceptors | interceptors | 로그인 페이지 이동 |
| ValidationError | UseCase / 훅 | feature 훅 | 필드별 에러 표시 |

---

## 금지 패턴

```ts
// ❌ 에러를 그냥 삼키기
try {
  await useCase.execute(input);
} catch (e) {
  console.log(e);  // 로그만 찍고 끝 - 사용자는 아무것도 모름
}

// ❌ 모든 에러를 같은 메시지로 처리
} catch (e) {
  setError('오류 발생');  // 도메인 에러인지 네트워크 오류인지 알 수 없음
}

// ❌ UseCase에서 에러를 삼키고 null 반환
async execute(): Promise<User | null> {
  try {
    return await this.repo.login(creds);
  } catch {
    return null;  // 에러를 숨김 - 훅에서 원인 파악 불가
  }
}
```
