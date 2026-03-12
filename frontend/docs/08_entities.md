# 🧬 Entity 설계 원칙

## 개요

Entity는 **도메인의 핵심 개념**을 표현하는 순수 TypeScript 타입/클래스다.  
외부 의존(axios, React, localStorage 등)이 전혀 없으며,  
비즈니스 규칙의 **언어** 그 자체다.

---

## 위치

```
features/{feature}/domain/entities/
├── User.ts
├── LoginCredentials.ts
├── Character.ts
├── Position.ts
├── Loan.ts
└── LoanRequest.ts
```

---

## Entity 작성 규칙

### 1. 순수 TypeScript interface / class만 사용
```ts
// ✅ 올바른 Entity
export interface User {
  readonly id: string;
  readonly name: string;
  readonly email: string;
  readonly creditScore: number;
  readonly createdAt: Date;
}

// ❌ 금지: 외부 라이브러리 import
import { Schema } from 'mongoose';    // 외부 의존 금지
import axios from 'axios';            // 외부 의존 금지
```

### 2. 불변성 (Readonly) 적용
Entity는 생성 후 변경되지 않는다.  
상태 변화가 필요하면 새 객체를 생성한다.

```ts
export interface Character {
  readonly id: string;
  readonly name: string;
  readonly position: Position;
  readonly state: CharacterState;
  readonly spriteKey: string;
}

// 이동 시 → 새 객체 반환 (UseCase에서 처리)
const movedCharacter: Character = {
  ...character,
  position: newPosition,
  state: 'walking',
};
```

### 3. 값 객체(Value Object) 분리
의미 있는 값은 별도 타입으로 분리한다.

```ts
// features/game/domain/entities/Position.ts
export interface Position {
  readonly x: number;
  readonly y: number;
}

// features/loan/domain/entities/Money.ts
export interface Money {
  readonly amount: number;
  readonly currency: 'KRW' | 'USD';
}

// features/auth/domain/entities/LoginCredentials.ts
export interface LoginCredentials {
  readonly email: string;
  readonly password: string;
}
```

---

## Entity에 비즈니스 규칙 포함 (선택)

단순한 검증 로직은 Entity 팩토리 함수로 포함할 수 있다.  
복잡한 규칙은 UseCase로 분리.

```ts
// features/loan/domain/entities/Loan.ts
export interface Loan {
  readonly id: string;
  readonly amount: number;
  readonly term: number;
  readonly status: LoanStatus;
  readonly appliedAt: Date;
}

export type LoanStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'REPAID';

// 도메인 규칙을 포함한 팩토리 함수
export function createLoan(amount: number, term: number): Loan {
  if (amount <= 0) throw new DomainError('대출 금액은 0보다 커야 합니다.');
  if (term < 6 || term > 60) throw new DomainError('대출 기간은 6~60개월이어야 합니다.');

  return {
    id: crypto.randomUUID(),
    amount,
    term,
    status: 'PENDING',
    appliedAt: new Date(),
  };
}
```

---

## Entity vs Model 구분

| 구분 | Entity | Model (DTO) |
|------|--------|-------------|
| 위치 | `domain/entities/` | `data/models/` |
| 목적 | 비즈니스 개념 표현 | API 요청/응답 형태 |
| 필드명 | camelCase | snake_case 허용 (API 그대로) |
| 의존 | 없음 | 없음 (순수 타입) |
| 변환 | — | Repository에서 Entity로 변환 |

```ts
// Entity (domain)
export interface User {
  id: string;
  name: string;
  email: string;
}

// Model (data) - API 응답 그대로
export interface UserModel {
  user_id: string;
  user_name: string;
  email_address: string;
}
```

---

## 브랜드 타입 (ID 혼동 방지)

```ts
// features/auth/domain/entities/UserId.ts
export type UserId = string & { readonly _brand: 'UserId' };
export type LoanId = string & { readonly _brand: 'LoanId' };

export function toUserId(id: string): UserId {
  return id as UserId;
}

// 사용 시 컴파일 타임에 혼동 방지
function getUser(id: UserId): Promise<User> { ... }
function getLoan(id: LoanId): Promise<Loan> { ... }

// getUser(loanId) ← 컴파일 에러
```

---

## 금지 패턴

```ts
// ❌ Entity에 메서드 과잉 (비즈니스 로직은 UseCase로)
export class User {
  async login() { await axios.post('/login'); }   // 완전 금지
  validate() { ... }                               // UseCase로 이동
}

// ❌ Entity에서 다른 계층 import
import { useAuthStore } from '@core/store/authStore';   // 금지
import { AuthRepositoryImpl } from '../../data/...';    // 금지

// ❌ mutable 필드
export interface User {
  id: string;     // readonly 빠짐 - 실수로 수정 가능
}
```
