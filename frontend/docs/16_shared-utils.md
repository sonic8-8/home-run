# 🛠️ shared/utils 설계 원칙

## utils란 무엇인가?

utils는 **어떤 컴포넌트나 훅도 아닌, 순수 함수들의 모음**이다.  
React를 모르고, 상태를 모르고, API를 모른다.  
**입력을 받아 출력을 돌려주는 것**만 한다.

```
utils가 맞는 것
  formatMoney(1500000) → "1,500,000원"   ← 숫자 넣으면 문자열 나옴
  isValidEmail('a@b')  → true/false       ← 문자열 넣으면 boolean 나옴
  formatDate(new Date) → "2026.03.11"    ← Date 넣으면 문자열 나옴

utils가 아닌 것
  login()              → API 호출 있음    → UseCase
  useModal()           → React 상태 있음 → hooks
  <Button />           → JSX 반환        → components
```

---

## 위치

```
shared/utils/
├── formatter.ts     # 표시용 포맷 변환 (금액, 날짜, 전화번호 등)
├── validator.ts     # 유효성 검사 (이메일, 전화번호, 금액 범위 등)
├── calculator.ts    # 순수 계산 (이자 계산, 날짜 계산 등)
└── storage.ts       # localStorage 래퍼 (key 상수 + 타입 안전 접근)
```

### "이것은 utils에 들어가야 하나?" 판단 기준

```
질문 1: React import가 필요한가?
  → YES: utils 아님 (hook 또는 component)

질문 2: API 호출이나 외부 의존이 있는가?
  → YES: utils 아님 (DataSource 또는 UseCase)

질문 3: 같은 입력이면 항상 같은 출력이 나오는 순수 함수인가?
  → YES: utils 적합

질문 4: 여러 feature에서 재사용되는가?
  → YES: shared/utils
  → NO (한 feature에서만): features/{f}/utils/ 에 위치 가능
```

---

## formatter.ts

화면에 보여줄 값을 **사람이 읽기 좋은 형태**로 변환한다.

```ts
// shared/utils/formatter.ts

// ── 금액 포맷 ──────────────────────────────────────────────────────────

/**
 * 숫자를 한국 금액 형식으로 변환
 * formatMoney(1500000)        → "1,500,000원"
 * formatMoney(1500000, false) → "1,500,000"
 */
export function formatMoney(amount: number, withUnit = true): string {
  const formatted = new Intl.NumberFormat('ko-KR').format(amount);
  return withUnit ? `${formatted}원` : formatted;
}

/**
 * 큰 금액을 한국식 단위로 축약
 * formatMoneyShort(150000000) → "1억 5천만원"
 * formatMoneyShort(50000)     → "5만원"
 */
export function formatMoneyShort(amount: number): string {
  const eok = Math.floor(amount / 100_000_000);
  const man = Math.floor((amount % 100_000_000) / 10_000);

  const parts: string[] = [];
  if (eok > 0) parts.push(`${eok}억`);
  if (man > 0) parts.push(`${man}천만`);
  if (parts.length === 0) parts.push(formatMoney(amount, false));

  return parts.join(' ') + '원';
}

// ── 날짜 포맷 ──────────────────────────────────────────────────────────

/**
 * Date를 "2026.03.11" 형태로 변환
 */
export function formatDate(date: Date): string {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(date).replace(/\. /g, '.').replace(/\.$/, '');
}

/**
 * Date를 "2026.03.11 14:30" 형태로 변환
 */
export function formatDateTime(date: Date): string {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(date);
}

/**
 * 현재 시간 기준 상대 시간 표시
 * formatRelativeTime(어제)    → "1일 전"
 * formatRelativeTime(1시간전) → "1시간 전"
 */
export function formatRelativeTime(date: Date): string {
  const diffMs = Date.now() - date.getTime();
  const diffMin = Math.floor(diffMs / 60_000);
  const diffHour = Math.floor(diffMin / 60);
  const diffDay = Math.floor(diffHour / 24);

  if (diffMin < 1) return '방금 전';
  if (diffMin < 60) return `${diffMin}분 전`;
  if (diffHour < 24) return `${diffHour}시간 전`;
  if (diffDay < 30) return `${diffDay}일 전`;
  return formatDate(date);
}

// ── 전화번호 포맷 ──────────────────────────────────────────────────────

/**
 * 숫자만 있는 전화번호에 하이픈 추가
 * formatPhone("01012345678") → "010-1234-5678"
 * formatPhone("0212345678")  → "02-1234-5678"
 */
export function formatPhone(phone: string): string {
  const digits = phone.replace(/\D/g, '');
  if (digits.startsWith('02')) {
    return digits.replace(/^(\d{2})(\d{3,4})(\d{4})$/, '$1-$2-$3');
  }
  return digits.replace(/^(\d{3})(\d{3,4})(\d{4})$/, '$1-$2-$3');
}

// ── 대출 관련 포맷 ─────────────────────────────────────────────────────

/**
 * 대출 기간 포맷
 * formatLoanTerm(12) → "12개월 (1년)"
 * formatLoanTerm(6)  → "6개월"
 */
export function formatLoanTerm(months: number): string {
  const years = Math.floor(months / 12);
  const remainMonths = months % 12;

  if (years === 0) return `${months}개월`;
  if (remainMonths === 0) return `${months}개월 (${years}년)`;
  return `${months}개월 (${years}년 ${remainMonths}개월)`;
}

/**
 * 이자율 포맷
 * formatRate(4.5) → "4.50%"
 */
export function formatRate(rate: number): string {
  return `${rate.toFixed(2)}%`;
}
```

---

## validator.ts

입력값이 올바른지 **true/false** 또는 **에러 메시지**를 반환한다.

```ts
// shared/utils/validator.ts

// ── 이메일 ──────────────────────────────────────────────────────────────

/**
 * isValidEmail("a@b.com") → true
 * isValidEmail("invalid")  → false
 */
export function isValidEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

// ── 전화번호 ────────────────────────────────────────────────────────────

/**
 * 한국 휴대폰 번호 형식 검사 (하이픈 유무 무관)
 * isValidPhone("010-1234-5678") → true
 * isValidPhone("01012345678")   → true
 */
export function isValidPhone(phone: string): boolean {
  const digits = phone.replace(/\D/g, '');
  return /^01[016789]\d{7,8}$/.test(digits);
}

// ── 비밀번호 ────────────────────────────────────────────────────────────

/**
 * 비밀번호 유효성 검사
 * 조건: 8자 이상, 영문 + 숫자 + 특수문자 각 1개 이상
 */
export function isValidPassword(password: string): boolean {
  return (
    password.length >= 8 &&
    /[A-Za-z]/.test(password) &&
    /\d/.test(password) &&
    /[!@#$%^&*]/.test(password)
  );
}

/**
 * 비밀번호 에러 메시지 반환 (유효하면 null)
 * 폼 유효성 검사에서 에러 메시지 표시용
 */
export function getPasswordError(password: string): string | null {
  if (password.length < 8) return '비밀번호는 8자 이상이어야 합니다.';
  if (!/[A-Za-z]/.test(password)) return '영문자를 포함해야 합니다.';
  if (!/\d/.test(password)) return '숫자를 포함해야 합니다.';
  if (!/[!@#$%^&*]/.test(password)) return '특수문자(!@#$%^&*)를 포함해야 합니다.';
  return null;
}

// ── 공백/빈값 ────────────────────────────────────────────────────────────

export function isEmpty(value: string | null | undefined): boolean {
  return value === null || value === undefined || value.trim() === '';
}

export function isNotEmpty(value: string | null | undefined): boolean {
  return !isEmpty(value);
}

// ── 숫자 ──────────────────────────────────────────────────────────────

/**
 * 콤마가 포함된 숫자 문자열인지 확인
 * isNumericString("1,000,000") → true
 */
export function isNumericString(value: string): boolean {
  return /^[\d,]+$/.test(value);
}
```

---

## calculator.ts

비즈니스 관련 **순수 계산 함수**들이다.

```ts
// shared/utils/calculator.ts

/**
 * 월 상환액 계산 (원리금 균등 방식)
 *
 * @param principal  대출 원금 (원)
 * @param annualRate 연 이자율 (예: 4.5 → 4.5%)
 * @param months     대출 기간 (개월)
 * @returns 월 상환액 (원, 올림)
 *
 * 사용 예시:
 *   calculateMonthlyPayment(10_000_000, 4.5, 12) → 856,000원 내외
 */
export function calculateMonthlyPayment(
  principal: number,
  annualRate: number,
  months: number
): number {
  if (annualRate === 0) return Math.ceil(principal / months);

  const monthlyRate = annualRate / 100 / 12;
  const payment =
    (principal * monthlyRate * Math.pow(1 + monthlyRate, months)) /
    (Math.pow(1 + monthlyRate, months) - 1);

  return Math.ceil(payment);
}

/**
 * 총 상환액 = 월 상환액 × 기간
 */
export function calculateTotalPayment(
  principal: number,
  annualRate: number,
  months: number
): number {
  return calculateMonthlyPayment(principal, annualRate, months) * months;
}

/**
 * 총 이자액 = 총 상환액 - 원금
 */
export function calculateTotalInterest(
  principal: number,
  annualRate: number,
  months: number
): number {
  return calculateTotalPayment(principal, annualRate, months) - principal;
}

/**
 * 두 날짜 사이의 일수
 * daysBetween(new Date('2026-01-01'), new Date('2026-03-11')) → 69
 */
export function daysBetween(start: Date, end: Date): number {
  const msPerDay = 1000 * 60 * 60 * 24;
  return Math.floor((end.getTime() - start.getTime()) / msPerDay);
}
```

---

## storage.ts

localStorage를 **타입 안전**하게 사용하기 위한 래퍼다.  
문자열 키를 상수로 관리해 오타 버그를 방지한다.

```ts
// shared/utils/storage.ts

// ── 키 상수 중앙 관리 ─────────────────────────────────────────────────
// ✅ 이렇게 상수로 쓰면 오타가 나도 컴파일 에러로 잡힘
// ❌ localStorage.getItem('acesToken') ← 오타를 런타임에야 발견
export const STORAGE_KEYS = {
  ACCESS_TOKEN: 'accessToken',
  REFRESH_TOKEN: 'refreshToken',
  GAME_SAVE: 'gameSave',
  USER_SETTINGS: 'userSettings',
} as const;

// ── 타입 안전 get/set ─────────────────────────────────────────────────

/**
 * localStorage에서 JSON 파싱하여 반환
 * 없거나 파싱 실패 시 null 반환 (앱 크래시 방지)
 *
 * 사용 예시:
 *   const token = getStorage<string>(STORAGE_KEYS.ACCESS_TOKEN);
 */
export function getStorage<T>(key: string): T | null {
  try {
    const item = localStorage.getItem(key);
    return item ? (JSON.parse(item) as T) : null;
  } catch {
    return null;
  }
}

/**
 * localStorage에 JSON으로 저장
 *
 * 사용 예시:
 *   setStorage(STORAGE_KEYS.ACCESS_TOKEN, 'eyJhbGci...');
 */
export function setStorage<T>(key: string, value: T): void {
  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch {
    console.warn(`[storage] '${key}' 저장 실패`);
  }
}

/**
 * localStorage에서 특정 키 제거
 */
export function removeStorage(key: string): void {
  localStorage.removeItem(key);
}

/**
 * 인증 관련 토큰 일괄 제거 (로그아웃 시 사용)
 */
export function clearAuthStorage(): void {
  removeStorage(STORAGE_KEYS.ACCESS_TOKEN);
  removeStorage(STORAGE_KEYS.REFRESH_TOKEN);
}
```

---

## utils 작성 체크리스트

새 유틸 함수를 작성하기 전 확인:

```
□ 순수 함수인가? (같은 입력 → 항상 같은 출력)
□ React import가 없는가?
□ API 호출이나 외부 의존이 없는가?
□ 여러 feature에서 쓰이는가? (아니면 feature 내부 utils로)
□ JSDoc 주석으로 입력/출력/사용 예시를 적었는가?
□ 함수명이 동사로 시작하는가? (format, is, calculate, get...)
```

---

## 금지 패턴

```ts
// ❌ utils에서 React 사용 → hooks으로 이동
import { useState } from 'react';
export function useFormatter() { ... }

// ❌ utils에서 API 호출 → DataSource로 이동
export async function fetchUser() {
  return axios.get('/user');
}

// ❌ 한 feature에서만 쓰이는 함수를 shared/utils에 넣기
// loan feature 전용이면 → features/loan/utils/loanFormatter.ts

// ❌ 키 문자열 하드코딩
localStorage.getItem('accessToken');     // STORAGE_KEYS 상수 사용
localStorage.setItem('refreshToken', t); // setStorage() 함수 사용
```
