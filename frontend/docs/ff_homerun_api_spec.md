# 홈런 (Home-Run) API 명세서

> **Base URL**: `/api`
> **기준 문서**: `01_homerun_game_design.md` v2.4, `02_homerun_requirements.md` v2.3
> **버전**: v2.2 (2026-03-12 — 상태값/용어 정합성 및 enum 정의 보강)
> **표현 방식**: Markdown에서는 `<details>` 접기 블록을 사용했고, Notion 반영 시 동일 단위를 Toggle Heading으로 옮기면 됩니다.

---

## 0. 공통 응답 값

### 0.1 주요 Enum

- `HousingType`: `NONE` (무주거), `STUDIO` (원룸/고시원), `VILLA` (빌라/오피스텔), `JEONSE_APT` (아파트 전세), `OWNED_APT` (아파트 자가)
- `EndingType`: `CLEAR`, `BANKRUPT`, `TIMEOUT`, `FORECLOSURE`

---

## 1. Auth & User (인증 및 유저)

### 1.1 소셜 로그인

| API | 설명 | 요청 요소 | 성공 |
|---|---|---|---|
| `POST /auth/login/kakao` | 카카오 로그인 | Body `authorizationCode` | `200` |
| `POST /auth/login/ssafy` | SSAFY 로그인 | Body `authorizationCode` | `200` |

#### `POST /auth/login/kakao` — 카카오 로그인

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/auth/login/kakao` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | `authorizationCode` |
| Success | `200` |
| Response | `accessToken`, `refreshToken`, `isNewUser` |

<details>
<summary>Request JSON</summary>

```json
{
  "authorizationCode": "카카오_인가코드"
}
```

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "isNewUser": true
}
```

</details>

#### `POST /auth/login/ssafy` — SSAFY 로그인

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/auth/login/ssafy` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | `authorizationCode` |
| Success | `200` |
| Response | `accessToken`, `refreshToken`, `isNewUser` |

<details>
<summary>Request JSON</summary>

```json
{
  "authorizationCode": "SSAFY_인가코드"
}
```

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "isNewUser": false
}
```

</details>

### 1.2 이메일 인증

| API | 설명 | 요청 요소 | 성공 |
|---|---|---|---|
| `POST /auth/signup` | 이메일 회원가입 | Body `name`, `email`, `password`, `passwordConfirm`, `termsAgreed` | `201` |
| `POST /auth/login` | 이메일 로그인 | Body `email`, `password` | `200` |
| `POST /auth/refresh` | 토큰 갱신 | Header | `200` |

#### `POST /auth/signup` — 이메일 회원가입

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/auth/signup` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | `name`, `email`, `password`, `passwordConfirm`, `termsAgreed` |
| Success | `201` |
| Response | `userId`, `email`, `name` |

<details>
<summary>Request JSON</summary>

```json
{
  "name": "김싸피",
  "email": "example@examl.com",
  "password": "P@ssw0rd!",
  "passwordConfirm": "P@ssw0rd!",
  "termsAgreed": true
}
```

</details>

<details>
<summary>Response `201` JSON</summary>

```json
{
  "userId": 1,
  "email": "example@examl.com",
  "name": "김싸피"
}
```

</details>

#### `POST /auth/login` — 이메일 로그인

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/auth/login` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | `email`, `password` |
| Success | `200` |
| Response | `accessToken`, `refreshToken` |

<details>
<summary>Request JSON</summary>

```json
{
  "email": "example@examl.com",
  "password": "P@ssw0rd!"
}
```

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

</details>

#### `POST /auth/refresh` — 토큰 갱신

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/auth/refresh` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | Header |
| Success | `200` |
| Response | `accessToken` |

<details>
<summary>Request Header</summary>

```http
Authorization: Bearer {refreshToken}
```

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

</details>

### 1.3 유저 프로필

| API | 설명 | 요청 요소 | 성공 |
|---|---|---|---|
| `PATCH /users/me/nickname` | 닉네임 설정 | Body `nickname` | `200` |
| `POST /users/me/ssafy-connect` | SSAFY 금융 연동 (마이데이터) | Body `userKey`, `apiKey` | `200` |

#### `PATCH /users/me/nickname` — 닉네임 설정

| 항목 | 내용 |
|---|---|
| Method | `PATCH` |
| Path | `/users/me/nickname` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | `nickname` |
| Success | `200` |
| Response | `userId`, `nickname` |

<details>
<summary>Request JSON</summary>

```json
{
  "nickname": "김싸피"
}
```

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "userId": 1,
  "nickname": "김싸피"
}
```

</details>

#### `POST /users/me/ssafy-connect` — SSAFY 금융 연동 (마이데이터)

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/users/me/ssafy-connect` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | `userKey`, `apiKey` |
| Success | `200` |
| Response | `connected`, `connectedAt` |

<details>
<summary>Request JSON</summary>

```json
{
  "userKey": "ssafy-user-key-xxx",
  "apiKey": "ssafy-api-key-xxx"
}
```

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "connected": true,
  "connectedAt": "2026-03-11T14:00:00"
}
```

</details>

---
## 2. Part A: Financial Home (금융 홈)

### 2.1 대시보드

| API | 설명 | 요청 요소 | 성공 |
|---|---|---|---|
| `GET /home/dashboard` | 금융 홈 요약 데이터 | 없음 | `200` |
| `GET /home/spending` | 카테고리별 소비 분석 | Query `month` | `200` |

#### `GET /home/dashboard` — 금융 홈 요약 데이터

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/home/dashboard` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `totalAssets`, `monthlyIncome`, `monthlyExpense`, `incomeChangeFromLastMonth`, `expenseChangeFromLastMonth` 외 1 |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "totalAssets": 42300000,
  "monthlyIncome": 2800000,
  "monthlyExpense": 1420000,
  "incomeChangeFromLastMonth": 0,
  "expenseChangeFromLastMonth": 220000,
  "nextPaydayDays": 7
}
```

</details>

#### `GET /home/spending` — 카테고리별 소비 분석

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/home/spending` |
| Path Params | 없음 |
| Query Params | `month` |
| Request | 없음 |
| Success | `200` |
| Response | `month`, `totalExpense`, `categories` |

<details>
<summary>Request Query</summary>

```text
?month=2026-03
```

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "month": "2026-03",
  "totalExpense": 1420000,
  "categories": [
    { "category": "식비", "amount": 450000, "ratio": 31.7 },
    { "category": "교통", "amount": 120000, "ratio": 8.5 },
    { "category": "쇼핑", "amount": 300000, "ratio": 21.1 }
  ]
}
```

</details>

### 2.2 시드머니 저축 계좌

| API | 설명 | 요청 요소 | 성공 |
|---|---|---|---|
| `GET /seedmoney/account` | 시드머니 계좌 조회 | 없음 | `200` |
| `POST /seedmoney/transfer` | 송금 | Body `toAccountNumber`, `amount` | `200` |
| `POST /seedmoney/deposit` | 입금 | Body `fromAccountNumber`, `amount` | `200` |

#### `GET /seedmoney/account` — 시드머니 계좌 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/seedmoney/account` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `bankName`, `accountNumber`, `balance` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "bankName": "싸피은행",
  "accountNumber": "110-123-000000",
  "balance": 300000
}
```

</details>

#### `POST /seedmoney/transfer` — 송금

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/seedmoney/transfer` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | `toAccountNumber`, `amount` |
| Success | `200` |
| Response | `transactionId`, `remainingBalance` |

<details>
<summary>Request JSON</summary>

```json
{
  "toAccountNumber": "110-456-000000",
  "amount": 50000
}
```

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "transactionId": "TXN-20260311-001",
  "remainingBalance": 250000
}
```

</details>

#### `POST /seedmoney/deposit` — 입금

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/seedmoney/deposit` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | `fromAccountNumber`, `amount` |
| Success | `200` |
| Response | `transactionId`, `remainingBalance` |

<details>
<summary>Request JSON</summary>

```json
{
  "fromAccountNumber": "110-789-000000",
  "amount": 100000
}
```

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "transactionId": "TXN-20260311-002",
  "remainingBalance": 400000
}
```

</details>

### 2.3 소비통제 PASS 시스템

| API | 설명 | 요청 요소 | 성공 |
|---|---|---|---|
| `GET /pass/products` | 전체 PASS 상품 목록 조회 | 없음 | `200` |
| `GET /pass/subscriptions` | 내 PASS 구독 목록 조회 | 없음 | `200` |
| `POST /pass/subscribe` | PASS 구독 신청 | Body `passId`, `sourceAccountId` | `201` |
| `DELETE /pass/subscriptions/{subscriptionId}` | PASS 구독 해지 | Path `subscriptionId` | `204 No Content` |
| `POST /pass/save` | 꾹 눌러 저축 (즉시 이체) | Body `subscriptionId`, `sourceAccountId` | `200` |
| `GET /pass/widget` | 오늘 절약 현황 위젯 | 없음 | `200` |
| `GET /pass/history` | 저축 내역 조회 | Query `page`, `size` | `200` |

#### `GET /pass/products` — 전체 PASS 상품 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/pass/products` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `products` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "products": [
    {
      "passId": 1,
      "name": "커피 PASS",
      "amountPerSave": 5000,
      "description": "커피 마시고 싶은 마음을 꾹 참고 저축해볼까요?"
    },
    {
      "passId": 2,
      "name": "배달 PASS",
      "amountPerSave": 20000,
      "description": "배달 시키고 싶은 마음을 꾹 참고 저축해볼까요?"
    },
    {
      "passId": 3,
      "name": "택시 PASS",
      "amountPerSave": 10000,
      "description": "택시 타고 싶은 마음을 꾹 참고 저축해볼까요?"
    }
  ]
}
```

</details>

#### `GET /pass/subscriptions` — 내 PASS 구독 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/pass/subscriptions` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `subscriptions` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "subscriptions": [
    {
      "subscriptionId": 1,
      "passId": 1,
      "name": "커피 PASS",
      "amountPerSave": 5000,
      "totalSaved": 65000,
      "weeklyHistory": [true, true, true, true, true, false, false]
    },
    {
      "subscriptionId": 2,
      "passId": 2,
      "name": "배달 PASS",
      "amountPerSave": 20000,
      "totalSaved": 65000,
      "weeklyHistory": [true, true, true, true, true, false, false]
    }
  ]
}
```

</details>

> `weeklyHistory`: 월~일 순서, 해당 요일 저축 여부

#### `POST /pass/subscribe` — PASS 구독 신청

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/pass/subscribe` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | `passId`, `sourceAccountId` |
| Success | `201` |
| Response | `subscriptionId`, `passId`, `name`, `subscribedAt` |

<details>
<summary>Request JSON</summary>

```json
{
  "passId": 3,
  "sourceAccountId": "110-123-000000"
}
```

</details>

<details>
<summary>Response `201` JSON</summary>

```json
{
  "subscriptionId": 3,
  "passId": 3,
  "name": "택시 PASS",
  "subscribedAt": "2026-03-11T14:00:00"
}
```

</details>

#### `DELETE /pass/subscriptions/{subscriptionId}` — PASS 구독 해지

| 항목 | 내용 |
|---|---|
| Method | `DELETE` |
| Path | `/pass/subscriptions/{subscriptionId}` |
| Path Params | `subscriptionId` |
| Query Params | 없음 |
| Request | 없음 |
| Success | `204 No Content` |
| Response | 없음 |

<details>
<summary>Response `204 No Content`</summary>

응답 본문 없음.

</details>

#### `POST /pass/save` — 꾹 눌러 저축 (즉시 이체)

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/pass/save` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | `subscriptionId`, `sourceAccountId` |
| Success | `200` |
| Response | `savedAmount`, `totalSaved`, `remainingBalance` |

<details>
<summary>Request JSON</summary>

```json
{
  "subscriptionId": 1,
  "sourceAccountId": "110-123-000000"
}
```

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "savedAmount": 5000,
  "totalSaved": 70000,
  "remainingBalance": 295000
}
```

</details>

#### `GET /pass/widget` — 오늘 절약 현황 위젯

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/pass/widget` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `todaySaved`, `weeklySaved`, `weeklyGoal`, `progressRate`, `remaining` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "todaySaved": 45000,
  "weeklySaved": 182000,
  "weeklyGoal": 500000,
  "progressRate": 36,
  "remaining": 318000
}
```

</details>

#### `GET /pass/history` — 저축 내역 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/pass/history` |
| Path Params | 없음 |
| Query Params | `page`, `size` |
| Request | 없음 |
| Success | `200` |
| Response | `content`, `page`, `size`, `totalElements`, `totalPages` |

<details>
<summary>Request Query</summary>

```text
?page=0&size=10
```

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "content": [
    {
      "historyId": 1,
      "passName": "커피 PASS",
      "amount": 5000,
      "savedAt": "2026-03-11T09:00:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 45,
  "totalPages": 5
}
```

</details>

### 2.4 신용정보 (점수형)

| API | 설명 | 요청 요소 | 성공 |
|---|---|---|---|
| `GET /credit/score` | 신용정보 조회 | 없음 | `200` |

#### `GET /credit/score` — 신용정보 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/credit/score` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `kcbScore`, `niceScore`, `baseScore`, `estimatedMinRate` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "kcbScore": 936,
  "niceScore": 948,
  "baseScore": 936,
  "estimatedMinRate": 4.89
}
```

</details>

### 2.5 금융 홈 추천

| API | 설명 | 요청 요소 | 성공 |
|---|---|---|---|
| `GET /home/loan-recommendations` | 대출 상품 추천 | 없음 | `200` |
| `GET /home/card-recommendations` | 카드 상품 추천 | 없음 | `200` |

#### `GET /home/loan-recommendations` — 대출 상품 추천

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/home/loan-recommendations` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `recommendations` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "recommendations": [
    {
      "productId": "LOAN-NH-001",
      "bankName": "NH",
      "bankLogoUrl": "/images/banks/nh.png",
      "productName": "NH 주택담보 대출",
      "productType": "주택담보대출",
      "minRate": 3.49,
      "maxRate": 5.89
    },
    {
      "productId": "LOAN-KB-001",
      "bankName": "KB",
      "bankLogoUrl": "/images/banks/kb.png",
      "productName": "KB 주택담보 대출",
      "productType": "주택담보대출",
      "minRate": 3.49,
      "maxRate": 5.89
    }
  ]
}
```

</details>

#### `GET /home/card-recommendations` — 카드 상품 추천

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/home/card-recommendations` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `recommendations` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "recommendations": [
    {
      "cardId": "CARD-SS-001",
      "cardName": "스타벅스 삼성카드",
      "cardImageUrl": "/images/cards/starbucks.png",
      "annualFee": 30000,
      "summary": "스타벅스 이용 시 스타벅스 별 리워드 적립"
    }
  ]
}
```

</details>

---
## 3. Part B: Game Core (게임 핵심)

### 3.1 세션 관리 (최대 3슬롯)

| API | 설명 | 요청 요소 | 성공 |
|---|---|---|---|
| `GET /games/sessions` | 내 세션 목록 조회 | 없음 | `200` |
| `POST /games/sessions` | 새 게임 세션 생성 | Body `slotNumber`, `characterType`, `characterName`, `jobType`, `useMyData` 외 3 | `201` |
| `GET /games/sessions/{id}` | 특정 세션 로드 (이어하기) | Path `id` | `200` |
| `DELETE /games/sessions/{id}` | 세션 삭제 | Path `id` | `204 No Content` |

#### `GET /games/sessions` — 내 세션 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/games/sessions` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `sessions` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "sessions": [
    {
      "sessionId": 1,
      "slotNumber": 1,
      "characterName": "김싸피",
      "characterType": "MALE",
      "jobType": "LARGE_BIZ",
      "currentTurn": 12,
      "totalAssets": 10000000,
      "createdAt": "2026-03-06",
      "status": "IN_PROGRESS"
    },
    {
      "sessionId": 2,
      "slotNumber": 2,
      "characterName": "김싸피",
      "characterType": "FEMALE",
      "jobType": "FREELANCER",
      "currentTurn": 3,
      "totalAssets": 1000,
      "createdAt": "2026-03-06",
      "status": "IN_PROGRESS"
    },
    {
      "sessionId": null,
      "slotNumber": 3,
      "status": "EMPTY"
    }
  ]
}
```

</details>

#### `POST /games/sessions` — 새 게임 세션 생성

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/games/sessions` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | `slotNumber`, `characterType`, `characterName`, `jobType`, `useMyData` 외 3 |
| Success | `201` |
| Response | `sessionId`, `characterName`, `characterType`, `jobType`, `jobTitle` 외 5 |

<details>
<summary>Request JSON</summary>

```json
{
  "slotNumber": 3,
  "characterType": "MALE",
  "characterName": "김싸피",
  "jobType": "LARGE_BIZ",
  "useMyData": true,
  "targetPropertyId": "PROP-HN-001",
  "regionCode": "SEOUL",
  "districtCode": "GANGNAM"
}
```

</details>

<details>
<summary>Response `201` JSON</summary>

```json
{
  "sessionId": 3,
  "characterName": "김싸피",
  "characterType": "MALE",
  "jobType": "LARGE_BIZ",
  "jobTitle": "대기업 직장인",
  "initialCash": 13000000,
  "monthlySalary": 2000000,
  "initialLoan": 0,
  "targetProperty": {
    "propertyId": "PROP-HN-001",
    "name": "하남3지구 모아엘가 더 퍼스트",
    "price": 375000000,
    "region": "서울",
    "district": "강남구"
  },
  "stats": {
    "health": 70,
    "fatigue": 0,
    "stress": 0,
    "knowledge": 50,
    "happiness": 50
  }
}
```

</details>

#### `GET /games/sessions/{id}` — 특정 세션 로드 (이어하기)

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/games/sessions/{id}` |
| Path Params | `id` |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `sessionId`, `currentTurn`, `currentDate`, `characterName`, `characterType` 외 8 |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "sessionId": 1,
  "currentTurn": 12,
  "currentDate": "2026-01-01",
  "characterName": "김싸피",
  "characterType": "MALE",
  "jobType": "LARGE_BIZ",
  "jobTitle": "대기업 직장인",
  "assets": {
    "cash": 1000000,
    "loan": 1000000,
    "realEstateValue": 0,
    "stockValue": 0,
    "netAssets": 0
  },
  "stats": {
    "knowledge": 70,
    "health": 30,
    "fatigue": 45,
    "stress": 15,
    "happiness": 90
  },
  "career": {
    "jobType": "STARTUP",
    "jobTitle": "스타트업 직장인",
    "annualSalary": 36000000,
    "tenure": 11
  },
  "creditScore": {
    "kcbScore": 936,
    "niceScore": 948,
    "estimatedMinRate": 4.89
  },
  "targetProperty": {
    "propertyId": "PROP-HN-001",
    "name": "하남3지구 모아엘가 더 퍼스트",
    "price": 375000000
  },
  "status": "IN_PROGRESS"
}
```

</details>

#### `DELETE /games/sessions/{id}` — 세션 삭제

| 항목 | 내용 |
|---|---|
| Method | `DELETE` |
| Path | `/games/sessions/{id}` |
| Path Params | `id` |
| Query Params | 없음 |
| Request | 없음 |
| Success | `204 No Content` |
| Response | 없음 |

<details>
<summary>Response `204 No Content`</summary>

응답 본문 없음.

</details>

### 3.2 게임 초기화 (선택지 조회)

| API | 설명 | 요청 요소 | 성공 |
|---|---|---|---|
| `GET /games/characters` | 캐릭터 목록 조회 | 없음 | `200` |
| `GET /games/job-types` | 직업 유형 목록 조회 | 없음 | `200` |
| `GET /games/regions` | 목표 지역 (시/도) 목록 조회 | 없음 | `200` |
| `GET /games/regions/{regionCode}/districts` | 구/군 목록 조회 | Path `regionCode` | `200` |
| `GET /games/regions/{regionCode}/districts/{districtCode}/properties` | 매물 목록 조회 | Path `regionCode`, `districtCode` | `200` |
| `POST /games/sessions/{id}/init-capital` | 초기 자본 세팅 (마이데이터 연동) | Path `id`<br>Body `useMyData` | `200` |

#### `GET /games/characters` — 캐릭터 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/games/characters` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `characters` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "characters": [
    {
      "characterType": "FEMALE",
      "thumbnailUrl": "/images/characters/female.png"
    },
    {
      "characterType": "MALE",
      "thumbnailUrl": "/images/characters/male.png"
    }
  ]
}
```

</details>

#### `GET /games/job-types` — 직업 유형 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/games/job-types` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `jobTypes` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "jobTypes": [
    {
      "jobType": "LARGE_BIZ",
      "label": "대기업 직장인",
      "stats": {
        "salary": 80,
        "health": 60,
        "stability": 90,
        "growthSpeed": 40,
        "difficulty": 70
      }
    },
    {
      "jobType": "MID_BIZ",
      "label": "중견기업 직장인",
      "stats": {
        "salary": 60,
        "health": 70,
        "stability": 70,
        "growthSpeed": 50,
        "difficulty": 50
      }
    },
    {
      "jobType": "SMALL_BIZ",
      "label": "중소기업 직장인",
      "stats": {
        "salary": 40,
        "health": 80,
        "stability": 50,
        "growthSpeed": 60,
        "difficulty": 30
      }
    },
    {
      "jobType": "STARTUP",
      "label": "스타트업 직장인",
      "stats": {
        "salary": 55,
        "health": 55,
        "stability": 35,
        "growthSpeed": 85,
        "difficulty": 80
      }
    },
    {
      "jobType": "FREELANCER",
      "label": "프리랜서",
      "stats": {
        "salary": 50,
        "health": 50,
        "stability": 20,
        "growthSpeed": 90,
        "difficulty": 85
      }
    }
  ]
}
```

</details>

> `stats` 값은 0~100 게이지 표시용

#### `GET /games/regions` — 목표 지역 (시/도) 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/games/regions` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `regions` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "regions": [
    { "regionCode": "SEOUL", "name": "서울" },
    { "regionCode": "GWANGJU", "name": "광주" }
  ]
}
```

</details>

#### `GET /games/regions/{regionCode}/districts` — 구/군 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/games/regions/{regionCode}/districts` |
| Path Params | `regionCode` |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `regionCode`, `districts` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "regionCode": "SEOUL",
  "districts": [
    { "districtCode": "GANGNAM", "name": "강남구" },
    { "districtCode": "SONGPA", "name": "송파구" },
    { "districtCode": "MAPO", "name": "마포구" },
    { "districtCode": "GWANGJIN", "name": "광진구" }
  ]
}
```

</details>

#### `GET /games/regions/{regionCode}/districts/{districtCode}/properties` — 매물 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/games/regions/{regionCode}/districts/{districtCode}/properties` |
| Path Params | `regionCode`, `districtCode` |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `properties` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "properties": [
    {
      "propertyId": "PROP-HN-001",
      "name": "하남3지구 모아엘가 더 퍼스트",
      "recentPrice": 375000000,
      "latitude": 37.5172,
      "longitude": 127.0473
    },
    {
      "propertyId": "PROP-HN-002",
      "name": "하남 포레스트 힐",
      "recentPrice": 420000000,
      "latitude": 37.5200,
      "longitude": 127.0500
    }
  ]
}
```

</details>

#### `POST /games/sessions/{id}/init-capital` — 초기 자본 세팅 (마이데이터 연동)

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/games/sessions/{id}/init-capital` |
| Path Params | `id` |
| Query Params | 없음 |
| Request | `useMyData` |
| Success | `200` |
| Response | `initialCash`, `monthlySalary`, `initialLoan`, `dataSource` |

<details>
<summary>Request JSON</summary>

```json
{
  "useMyData": true
}
```

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "initialCash": 13000000,
  "monthlySalary": 2000000,
  "initialLoan": 0,
  "dataSource": "MY_DATA"
}
```

</details>

---
## 4. Part B: Game Cycle (턴 진행)

### 4.1 턴 흐름

| API | 설명 | 요청 요소 | 성공 |
|---|---|---|---|
| `GET /games/sessions/{id}/turn` | 현재 턴 상태/뉴스 조회 | Path `id` | `200` |

#### `GET /games/sessions/{id}/turn` — 현재 턴 상태/뉴스 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/games/sessions/{id}/turn` |
| Path Params | `id` |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `turnNumber`, `currentDate`, `month`, `economicCycle`, `news` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "turnNumber": 12,
  "currentDate": "2026-01-01",
  "month": 3,
  "economicCycle": {
    "phase": "BOOM",
    "description": "경기 호황기"
  },
  "news": [
    {
      "newsId": 1,
      "headline": "부동산 시장 활황세 지속",
      "content": "수도권 아파트 가격이...",
      "type": "FORESHADOW",
      "effects": ["REAL_ESTATE_UP"]
    }
  ]
}
```

</details>

### 4.2 한달 스케줄 설정

| API | 설명 | 요청 요소 | 성공 |
|---|---|---|---|
| `GET /games/sessions/{id}/turn/actions` | 선택 가능 행동 목록 조회 | Path `id` | `200` |
| `POST /games/sessions/{id}/turn/slots` | 슬롯 행동 3개 일괄 제출 | Path `id`<br>Body `slots` | `200` |
| `POST /games/sessions/{id}/turn/commit` | 턴 종료 및 정산 | Path `id` | `200` |

#### `GET /games/sessions/{id}/turn/actions` — 선택 가능 행동 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/games/sessions/{id}/turn/actions` |
| Path Params | `id` |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `shopping`, `activities` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "shopping": [
    {
      "actionType": "COFFEE",
      "label": "커피",
      "iconUrl": "/images/actions/coffee.png",
      "effects": { "cash": -10000, "health": 10 }
    },
    {
      "actionType": "SHOPPING_BAG",
      "label": "쇼핑",
      "iconUrl": "/images/actions/shopping.png",
      "effects": { "cash": -10000, "health": 10 }
    },
    {
      "actionType": "BOOK",
      "label": "독서",
      "iconUrl": "/images/actions/book.png",
      "effects": { "cash": -10000, "knowledge": 10 }
    }
  ],
  "activities": [
    {
      "actionType": "STUDY",
      "label": "공부",
      "iconUrl": "/images/actions/study.png",
      "effects": { "cash": -10000, "knowledge": 10 }
    },
    {
      "actionType": "EXERCISE",
      "label": "운동",
      "iconUrl": "/images/actions/exercise.png",
      "effects": { "cash": -10000, "health": 10 }
    },
    {
      "actionType": "REST",
      "label": "휴식",
      "iconUrl": "/images/actions/rest.png",
      "effects": { "cash": -10000, "health": 10 }
    }
  ]
}
```

</details>

> 카테고리별(쇼핑/활동) 3개씩 선택, 좌우 스크롤로 추가 행동 탐색

#### `POST /games/sessions/{id}/turn/slots` — 슬롯 행동 3개 일괄 제출

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/games/sessions/{id}/turn/slots` |
| Path Params | `id` |
| Query Params | 없음 |
| Request | `slots` |
| Success | `200` |
| Response | `preview` |

<details>
<summary>Request JSON</summary>

```json
{
  "slots": [
    { "slotIndex": 0, "actionType": "COFFEE" },
    { "slotIndex": 1, "actionType": "STUDY" },
    { "slotIndex": 2, "actionType": "EXERCISE" }
  ]
}
```

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "preview": {
    "cashChange": -30000,
    "statChanges": {
      "health": 20,
      "knowledge": 10,
      "happiness": 5
    }
  }
}
```

</details>

#### `POST /games/sessions/{id}/turn/commit` — 턴 종료 및 정산

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/games/sessions/{id}/turn/commit` |
| Path Params | `id` |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `turnNumber`, `settlementLog`, `updatedAssets`, `updatedStats`, `flags` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "turnNumber": 12,
  "settlementLog": [
    { "phase": "SALARY", "description": "월급 수령", "cashChange": 2000000 },
    { "phase": "LOAN_INTEREST", "description": "대출 이자 납부", "cashChange": -30000 },
    { "phase": "MAINTENANCE", "description": "관리비 납부", "cashChange": -150000 },
    { "phase": "ACTION_RESULT", "description": "행동 결과 반영", "statChanges": { "health": 20, "knowledge": 10 } }
  ],
  "updatedAssets": {
    "cash": 2820000,
    "loan": 1000000,
    "realEstateValue": 0,
    "netAssets": 1820000
  },
  "updatedStats": {
    "knowledge": 80,
    "health": 50,
    "fatigue": 30,
    "stress": 20,
    "happiness": 85
  },
  "flags": {
    "isBankrupt": false,
    "isCleared": false,
    "isBurnout": false,
    "isForcedResignation": false,
    "hasEvent": true
  }
}
```

</details>

---
## 5. Part B: Financial Activities (게임 내 금융 활동)

### 5.1 내 자산 상세

| API | 설명 | 요청 요소 | 성공 |
|---|---|---|---|
| `GET /games/sessions/{id}/assets` | 내 자산 종합 조회 (내자산 탭) | Path `id` | `200` |
| `GET /games/sessions/{id}/news/history` | 지난 뉴스 조회 (지난뉴스 탭) | Path `id` | `200` |

#### `GET /games/sessions/{id}/assets` — 내 자산 종합 조회 (내자산 탭)

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/games/sessions/{id}/assets` |
| Path Params | `id` |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `cash`, `realEstate`, `loan`, `stock`, `career` 외 1 |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "cash": 1000000,
  "realEstate": {
    "propertyName": "하남3지구 모아엘가 더 퍼스트",
    "housingType": "JEONSE_APT",
    "currentValue": 375000000
  },
  "loan": {
    "principal": 1000000,
    "monthlyInterest": 30000
  },
  "stock": {
    "totalValue": 100000,
    "holdings": [
      { "stockCode": "BIO", "stockName": "바이오주", "value": 100000, "quantity": 11 }
    ]
  },
  "career": {
    "characterName": "김싸피",
    "jobType": "STARTUP",
    "jobTitle": "스타트업 직장인",
    "annualSalary": 36000000
  },
  "sideJobs": [
    {
      "sideJobId": 1,
      "name": "인형 눈 붙이기",
      "cashEffect": 100000,
      "healthEffect": -5
    },
    {
      "sideJobId": 2,
      "name": "배달 아르바이트",
      "cashEffect": 300000,
      "healthEffect": -10
    }
  ]
}
```

</details>

#### `GET /games/sessions/{id}/news/history` — 지난 뉴스 조회 (지난뉴스 탭)

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/games/sessions/{id}/news/history` |
| Path Params | `id` |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `news` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "news": [
    {
      "newsId": 1,
      "turnNumber": 10,
      "headline": "금리 인하 기조 지속",
      "content": "한국은행이...",
      "publishedDate": "2025-11-01"
    }
  ]
}
```

</details>

### 5.2 주식 거래 (`/games/sessions/{id}/stocks`)

| API | 설명 | 요청 요소 | 성공 |
|---|---|---|---|
| `GET .../stocks/market` | 주식 시장 현재가 목록 | 없음 | `200` |
| `GET .../stocks/holdings` | 내 보유 주식 현황 | 없음 | `200` |
| `POST .../stocks/orders` | 매수/매도 주문 | Body `stockCode`, `orderType`, `quantity` | `200` |

#### `GET .../stocks/market` — 주식 시장 현재가 목록

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `.../stocks/market` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `stocks` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "stocks": [
    {
      "stockCode": "BIO",
      "stockName": "바이오주",
      "currentPrice": 100000,
      "pricePerShare": "100,000 원 / 주"
    },
    {
      "stockCode": "BATTERY",
      "stockName": "이차전지주",
      "currentPrice": 100000,
      "pricePerShare": "100,000 원 / 주"
    },
    {
      "stockCode": "SEMI",
      "stockName": "반도체주",
      "currentPrice": 100000,
      "pricePerShare": "100,000 원 / 주"
    },
    {
      "stockCode": "FINANCE",
      "stockName": "금융주",
      "currentPrice": 100000,
      "pricePerShare": "100,000 원 / 주"
    },
    {
      "stockCode": "AUTO",
      "stockName": "자동차주",
      "currentPrice": 100000,
      "pricePerShare": "100,000 원 / 주"
    }
  ]
}
```

</details>

#### `GET .../stocks/holdings` — 내 보유 주식 현황

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `.../stocks/holdings` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `totalValue`, `totalReturnRate`, `totalPurchaseAmount`, `holdings` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "totalValue": 200000,
  "totalReturnRate": -80,
  "totalPurchaseAmount": 800000,
  "holdings": [
    {
      "stockCode": "BIO",
      "stockName": "바이오 주",
      "currentValue": 100000,
      "quantity": 10,
      "avgPurchasePrice": 80000,
      "returnRate": 25.0
    },
    {
      "stockCode": "SEMI",
      "stockName": "반도체 주",
      "currentValue": 100000,
      "quantity": 10,
      "avgPurchasePrice": 80000,
      "returnRate": 25.0
    }
  ]
}
```

</details>

#### `POST .../stocks/orders` — 매수/매도 주문

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `.../stocks/orders` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | `stockCode`, `orderType`, `quantity` |
| Success | `200` |
| Response | `orderId`, `stockCode`, `orderType`, `quantity`, `pricePerShare` 외 3 |

<details>
<summary>Request JSON</summary>

```json
{
  "stockCode": "BIO",
  "orderType": "BUY",
  "quantity": 5
}
```

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "orderId": "ORD-001",
  "stockCode": "BIO",
  "orderType": "BUY",
  "quantity": 5,
  "pricePerShare": 100000,
  "totalAmount": 500000,
  "executedAt": "2026-01-01T10:00:00",
  "remainingCash": 500000
}
```

</details>

### 5.3 대출 (`/games/sessions/{id}/loans`)

| API | 설명 | 요청 요소 | 성공 |
|---|---|---|---|
| `GET .../loans/products` | 대출 상품 목록 조회 | Query `category`, `page`, `size` | `200` |
| `GET .../loans/products/{productId}` | 대출 상품 상세 조회 | Path `productId` | `200` |
| `POST .../loans/calculate` | 이자 계산기 | Body `repaymentMethod`, `termMonths`, `principal`, `annualRate` | `200` |
| `POST .../loans/apply` | 대출 심사 신청 (Step 1→2: 매물 선택 → 심사) | Body `productId`, `propertyId` | `200` |
| `POST .../loans/confirm` | 대출 최종 신청 (Step 2→3: 금액 입력 → 계약) | Body `applicationId`, `requestedAmount`, `agreed` | `200` |
| `POST .../loans/repay` | 중도 일시 상환 | Body `loanId`, `amount` | `200` |

#### `GET .../loans/products` — 대출 상품 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `.../loans/products` |
| Path Params | 없음 |
| Query Params | `category`, `page`, `size` |
| Request | 없음 |
| Success | `200` |
| Response | `content`, `page`, `size`, `totalElements`, `totalPages` |

<details>
<summary>Request Query</summary>

```text
?category=ALL&page=0&size=10
```

> category: ALL | CREDIT | MORTGAGE | RECOMMENDED

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "content": [
    {
      "productId": "LOAN-NH-001",
      "bankName": "NH",
      "bankLogoUrl": "/images/banks/nh.png",
      "productName": "NH 주택담보 대출",
      "productType": "주택담보대출",
      "repaymentType": "원리금 균등상환",
      "maxAmount": 1000000000,
      "maxTerm": "50년",
      "minRate": 3.49,
      "maxRate": 5.89
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 25,
  "totalPages": 3
}
```

</details>

#### `GET .../loans/products/{productId}` — 대출 상품 상세 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `.../loans/products/{productId}` |
| Path Params | `productId` |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `productId`, `bankName`, `bankLogoUrl`, `productName`, `productType` 외 6 |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "productId": "LOAN-NH-001",
  "bankName": "NH",
  "bankLogoUrl": "/images/banks/nh.png",
  "productName": "NH 주택담보 대출",
  "productType": "주택담보대출",
  "repaymentType": "원리금 균등상환",
  "maxAmount": 1000000000,
  "maxTerm": "50년",
  "minRate": 3.49,
  "maxRate": 5.89,
  "features": ["원리금 균등상환", "최대 10억까지", "최대 50년까지"]
}
```

</details>

#### `POST .../loans/calculate` — 이자 계산기

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `.../loans/calculate` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | `repaymentMethod`, `termMonths`, `principal`, `annualRate` |
| Success | `200` |
| Response | `monthlyPayment`, `totalInterest`, `totalPayment` |

<details>
<summary>Request JSON</summary>

```json
{
  "repaymentMethod": "EQUAL_PRINCIPAL_INTEREST",
  "termMonths": 360,
  "principal": 200000000,
  "annualRate": 3.49
}
```

> repaymentMethod: EQUAL_PRINCIPAL_INTEREST | EQUAL_PRINCIPAL | BULLET

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "monthlyPayment": 897000,
  "totalInterest": 122920000,
  "totalPayment": 322920000
}
```

</details>

#### `POST .../loans/apply` — 대출 심사 신청 (Step 1→2: 매물 선택 → 심사)

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `.../loans/apply` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | `productId`, `propertyId` |
| Success | `200` |
| Response | `applicationId`, `status`, `requestInfo`, `result` |

<details>
<summary>Request JSON</summary>

```json
{
  "productId": "LOAN-NH-001",
  "propertyId": "PROP-HN-001"
}
```

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "applicationId": "APP-001",
  "status": "APPROVED",
  "requestInfo": {
    "propertyName": "하남3지구 모아엘가 더 퍼스트",
    "propertyPrice": 375000000,
    "applicationDate": "2026-03-10"
  },
  "result": {
    "maxLoanAmount": 200000000
  }
}
```

</details>

> 심사 거부 시 `status: "REJECTED"`, `result.rejectionReason` 포함

#### `POST .../loans/confirm` — 대출 최종 신청 (Step 2→3: 금액 입력 → 계약)

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `.../loans/confirm` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | `applicationId`, `requestedAmount`, `agreed` |
| Success | `200` |
| Response | `loanId`, `amount`, `annualRate`, `monthlyPayment`, `contractDate` 외 1 |

<details>
<summary>Request JSON</summary>

```json
{
  "applicationId": "APP-001",
  "requestedAmount": 150000000,
  "agreed": true
}
```

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "loanId": "LOAN-ACT-001",
  "amount": 150000000,
  "annualRate": 3.49,
  "monthlyPayment": 672000,
  "contractDate": "2026-03-10",
  "status": "ACTIVE"
}
```

</details>

#### `POST .../loans/repay` — 중도 일시 상환

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `.../loans/repay` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | `loanId`, `amount` |
| Success | `200` |
| Response | `loanId`, `repaidAmount`, `remainingPrincipal`, `updatedMonthlyPayment` |

<details>
<summary>Request JSON</summary>

```json
{
  "loanId": "LOAN-ACT-001",
  "amount": 50000000
}
```

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "loanId": "LOAN-ACT-001",
  "repaidAmount": 50000000,
  "remainingPrincipal": 100000000,
  "updatedMonthlyPayment": 448000
}
```

</details>

### 5.4 신용카드 (`/games/sessions/{id}/cards`)

| API | 설명 | 요청 요소 | 성공 |
|---|---|---|---|
| `GET .../cards/available` | 카드 상품 목록 조회 | Query `category`, `page`, `size` | `200` |
| `GET .../cards` | 내 보유 카드 조회 | 없음 | `200` |
| `POST .../cards` | 카드 등록 (최대 2장) | Body `cardId` | `201` |
| `DELETE .../cards/{userCardId}` | 카드 해지 | Path `userCardId` | `204 No Content` |

#### `GET .../cards/available` — 카드 상품 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `.../cards/available` |
| Path Params | 없음 |
| Query Params | `category`, `page`, `size` |
| Request | 없음 |
| Success | `200` |
| Response | `content`, `page`, `size`, `totalElements`, `totalPages` |

<details>
<summary>Request Query</summary>

```text
?category=ALL&page=0&size=8
```

> category: ALL | TRAVEL | GAS | LIVING | RECOMMENDED

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "content": [
    {
      "cardId": "CARD-SS-001",
      "cardName": "스타벅스 삼성카드",
      "cardImageFrontUrl": "/images/cards/starbucks-front.png",
      "cardImageBackUrl": "/images/cards/starbucks-back.png",
      "annualFee": 30000,
      "summary": "스타벅스 이용 시 스타벅스 별 리워드 적립",
      "benefits": [
        "스타벅스 이용 시 스타벅스 별 리워드 적립"
      ],
      "rewardConditions": [
        {
          "condition": "전월 이용 금액 - 50만원 미만",
          "detail": "당월 스타벅스 누적 이용금액 3만원당 스타벅스 별 리워드 1개 적립"
        },
        {
          "condition": "전월 이용 금액 - 50만원 이상",
          "detail": "당월 스타벅스 누적 이용금액 1만원당 스타벅스 별 리워드 5개 적립"
        }
      ],
      "rewardLimit": 50
    }
  ],
  "page": 0,
  "size": 8,
  "totalElements": 25,
  "totalPages": 4
}
```

</details>

#### `GET .../cards` — 내 보유 카드 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `.../cards` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `cards`, `maxCards` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "cards": [
    {
      "userCardId": 1,
      "cardId": "CARD-SS-001",
      "cardName": "스타벅스 삼성카드",
      "cardImageFrontUrl": "/images/cards/starbucks-front.png",
      "registeredAt": "2026-01-15"
    }
  ],
  "maxCards": 2
}
```

</details>

#### `POST .../cards` — 카드 등록 (최대 2장)

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `.../cards` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | `cardId` |
| Success | `201` |
| Response | `userCardId`, `cardId`, `cardName`, `registeredAt` |

<details>
<summary>Request JSON</summary>

```json
{
  "cardId": "CARD-SS-001"
}
```

</details>

<details>
<summary>Response `201` JSON</summary>

```json
{
  "userCardId": 1,
  "cardId": "CARD-SS-001",
  "cardName": "스타벅스 삼성카드",
  "registeredAt": "2026-01-15"
}
```

</details>

#### `DELETE .../cards/{userCardId}` — 카드 해지

| 항목 | 내용 |
|---|---|
| Method | `DELETE` |
| Path | `.../cards/{userCardId}` |
| Path Params | `userCardId` |
| Query Params | 없음 |
| Request | 없음 |
| Success | `204 No Content` |
| Response | 없음 |

<details>
<summary>Response `204 No Content`</summary>

응답 본문 없음.

</details>

### 5.5 부동산 및 계약 (`/games/sessions/{id}/real-estate`)

| API | 설명 | 요청 요소 | 성공 |
|---|---|---|---|
| `GET .../real-estate/properties` | 부동산 매물 목록 (지도 표시용) | Query `bounds` | `200` |
| `GET .../real-estate/properties/{propertyId}` | 매물 상세 조회 | Path `propertyId` | `200` |
| `POST .../real-estate/properties/{propertyId}/purchase` | 매물 구매 | Path `propertyId`<br>Body `loanId` | `200` |
| `GET .../real-estate/properties/{propertyId}/documents` | 서류 검토용 문건 조회 | Path `propertyId` | `200` |
| `POST .../real-estate/properties/{propertyId}/contract` | 서류 검토 완료 후 계약 | Path `propertyId`<br>Body `checkedTraps` | `200` |
| `DELETE .../real-estate/contract` | 현재 주거 매도/퇴거 | 없음 | `200` |

#### `GET .../real-estate/properties` — 부동산 매물 목록 (지도 표시용)

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `.../real-estate/properties` |
| Path Params | 없음 |
| Query Params | `bounds` |
| Request | 없음 |
| Success | `200` |
| Response | `properties` |

<details>
<summary>Request Query</summary>

```text
?bounds=37.51,127.04,37.53,127.06
```

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "properties": [
    {
      "propertyId": "PROP-HN-001",
      "name": "하남3지구 모아엘가 더 퍼스트",
      "recentPrice": 375000000,
      "latitude": 37.5172,
      "longitude": 127.0473
    },
    {
      "propertyId": "PROP-HN-002",
      "name": "하남부영사랑으로 3차아파트",
      "recentPrice": 280000000,
      "latitude": 37.5150,
      "longitude": 127.0400
    }
  ]
}
```

</details>

#### `GET .../real-estate/properties/{propertyId}` — 매물 상세 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `.../real-estate/properties/{propertyId}` |
| Path Params | `propertyId` |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `propertyId`, `name`, `recentPrice`, `address`, `latitude` 외 5 |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "propertyId": "PROP-HN-001",
  "name": "하남3지구 모아엘가 더 퍼스트",
  "recentPrice": 375000000,
  "address": "경기도 하남시 ...",
  "latitude": 37.5172,
  "longitude": 127.0473,
  "housingType": "OWNED_APT",
  "deposit": 0,
  "maintenanceFee": 250000,
  "specs": {
    "area": 84.5,
    "floor": "15/25",
    "direction": "남향"
  }
}
```

</details>

#### `POST .../real-estate/properties/{propertyId}/purchase` — 매물 구매

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `.../real-estate/properties/{propertyId}/purchase` |
| Path Params | `propertyId` |
| Query Params | 없음 |
| Request | `loanId` |
| Success | `200` |
| Response | `contractId`, `propertyId`, `propertyName`, `purchasePrice`, `loanAmount` 외 3 |

<details>
<summary>Request JSON</summary>

```json
{
  "loanId": "LOAN-ACT-001"
}
```

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "contractId": "CONTRACT-001",
  "propertyId": "PROP-HN-001",
  "propertyName": "하남3지구 모아엘가 더 퍼스트",
  "purchasePrice": 375000000,
  "loanAmount": 150000000,
  "selfFunded": 225000000,
  "remainingCash": 100000,
  "housingType": "OWNED_APT"
}
```

</details>

#### `GET .../real-estate/properties/{propertyId}/documents` — 서류 검토용 문건 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `.../real-estate/properties/{propertyId}/documents` |
| Path Params | `propertyId` |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `documents` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "documents": [
    {
      "documentId": 1,
      "type": "등기사항전부증명서",
      "imageUrl": "/images/docs/registry.png",
      "checklist": [
        { "trapId": "TRAP-01", "label": "근저당 설정 확인", "isTrapped": true },
        { "trapId": "TRAP-02", "label": "소유자 일치 확인", "isTrapped": false }
      ]
    }
  ]
}
```

</details>

#### `POST .../real-estate/properties/{propertyId}/contract` — 서류 검토 완료 후 계약

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `.../real-estate/properties/{propertyId}/contract` |
| Path Params | `propertyId` |
| Query Params | 없음 |
| Request | `checkedTraps` |
| Success | `200` |
| Response | `success`, `trapsDetected`, `trapsCorrectlyIdentified`, `contractResult`, `message` |

<details>
<summary>Request JSON</summary>

```json
{
  "checkedTraps": ["TRAP-01"]
}
```

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "success": true,
  "trapsDetected": 1,
  "trapsCorrectlyIdentified": 1,
  "contractResult": "SAFE",
  "message": "서류 검토를 통과했습니다."
}
```

</details>

#### `DELETE .../real-estate/contract` — 현재 주거 매도/퇴거

| 항목 | 내용 |
|---|---|
| Method | `DELETE` |
| Path | `.../real-estate/contract` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `refundedDeposit`, `updatedCash`, `previousHousingType`, `currentHousingType` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "refundedDeposit": 50000000,
  "updatedCash": 51000000,
  "previousHousingType": "JEONSE_APT",
  "currentHousingType": "NONE"
}
```

</details>

---
## 6. Part B: Events & Career (이벤트 및 직업)

### 6.1 커리어 (`/games/sessions/{id}/career`)

| API | 설명 | 요청 요소 | 성공 |
|---|---|---|---|
| `POST .../career/negotiate` | 연봉 협상 시도 (연 1회) | 없음 | `200` |
| `GET .../career/job-offers` | 이직 가능 풀 조회 | 없음 | `200` |
| `POST .../career/transfer` | 이직 수락 | Body `offerId` | `200` |

#### `POST .../career/negotiate` — 연봉 협상 시도 (연 1회)

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `.../career/negotiate` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `success`, `previousSalary`, `newSalary`, `raiseRate`, `message` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "success": true,
  "previousSalary": 36000000,
  "newSalary": 39600000,
  "raiseRate": 10.0,
  "message": "연봉 협상에 성공했습니다!"
}
```

</details>

#### `GET .../career/job-offers` — 이직 가능 풀 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `.../career/job-offers` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `offers` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "offers": [
    {
      "offerId": "OFFER-001",
      "companyName": "OO 대기업",
      "jobType": "LARGE_BIZ",
      "offeredSalary": 60000000,
      "currentSalary": 40000000
    }
  ]
}
```

</details>

#### `POST .../career/transfer` — 이직 수락

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `.../career/transfer` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | `offerId` |
| Success | `200` |
| Response | `previousJobType`, `newJobType`, `newJobTitle`, `newSalary`, `tenureReset` 외 1 |

<details>
<summary>Request JSON</summary>

```json
{
  "offerId": "OFFER-001"
}
```

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "previousJobType": "STARTUP",
  "newJobType": "LARGE_BIZ",
  "newJobTitle": "대기업 직장인",
  "newSalary": 60000000,
  "tenureReset": true,
  "message": "OO 대기업으로 이직했습니다."
}
```

</details>

### 6.2 이벤트 (`/games/sessions/{id}/events`)

| API | 설명 | 요청 요소 | 성공 |
|---|---|---|---|
| `GET .../events/pending` | 대기 중인 이벤트 조회 | 없음 | `200` |
| `POST .../events/{eventId}/resolve` | 이벤트 선택/수령 제출 | Path `eventId`<br>Body `choiceId` | `200` |

#### `GET .../events/pending` — 대기 중인 이벤트 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `.../events/pending` |
| Path Params | 없음 |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `events` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "events": [
    {
      "eventId": 1,
      "type": "GIFT",
      "title": "착한아이에게는 선물을",
      "description": "산타 할아버지는 우는아이에게 선물을 안주신대요...",
      "imageUrl": "/images/events/santa.png",
      "choices": null
    },
    {
      "eventId": 2,
      "type": "CHOICE",
      "title": "호외요 호외",
      "description": "정부에서 3차 상생지원금을 지원한다고 합니다...",
      "imageUrl": "/images/events/megaphone.png",
      "choices": [
        { "choiceId": "A", "label": "신청하기" },
        { "choiceId": "B", "label": "거절하기" }
      ]
    },
    {
      "eventId": 3,
      "type": "JOB_TRANSFER",
      "title": "열심히 일한 당신! 이직하시겠습니까?",
      "sender": "OO 기업 인사팀",
      "receiver": "김싸피 님",
      "date": "2026-03-09",
      "description": "열심히 일한 당신에게 OO 대기업에서 이직 오퍼가 도착했습니다.",
      "offeredSalary": 60000000,
      "currentSalary": 40000000,
      "choices": [
        { "choiceId": "ACCEPT", "label": "승인하기" },
        { "choiceId": "REJECT", "label": "거절하기" }
      ]
    },
    {
      "eventId": 4,
      "type": "PHONE",
      "title": "보이스피싱",
      "description": "알 수 없는 번호로 전화가 오고 있습니다.",
      "imageUrl": "/images/events/phone.png",
      "choices": [
        { "choiceId": "A", "label": "받기" },
        { "choiceId": "B", "label": "거절" }
      ]
    },
    {
      "eventId": 5,
      "type": "LETTER",
      "title": "누군가가 당신에게 편지를 보냈습니다.",
      "imageUrl": "/images/events/letter.png",
      "choices": null
    }
  ]
}
```

</details>

> `type` 종류: `GIFT` (수령만), `CHOICE` (2지선다), `JOB_TRANSFER` (이직 오퍼), `PHONE` (보이스피싱 등), `LETTER` (편지/알림)

#### `POST .../events/{eventId}/resolve` — 이벤트 선택/수령 제출

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `.../events/{eventId}/resolve` |
| Path Params | `eventId` |
| Query Params | 없음 |
| Request | `choiceId` |
| Success | `200` |
| Response | `eventId`, `resolvedChoice`, `result` |

<details>
<summary>Request JSON</summary>

```json
{
  "choiceId": "A"
}
```

> choiceId가 없는 이벤트(GIFT, LETTER): { "choiceId": null }

</details>

<details>
<summary>Response `200` JSON</summary>

```json
{
  "eventId": 2,
  "resolvedChoice": "A",
  "result": {
    "title": "상생지원금 수령",
    "description": "300,000원이 지급되었습니다.",
    "effects": {
      "cashChange": 300000,
      "statChanges": {}
    }
  }
}
```

</details>

---
## 7. Part B: Ending & Report (엔딩)

| API | 설명 | 요청 요소 | 성공 |
|---|---|---|---|
| `GET /games/sessions/{id}/ending` | 엔딩 리포트 데이터 조회 | Path `id` | `200` |
| `GET /games/sessions/{id}/logs` | 세션 히스토리 시계열 데이터 | Path `id` | `200` |

#### `GET /games/sessions/{id}/ending` — 엔딩 리포트 데이터 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/games/sessions/{id}/ending` |
| Path Params | `id` |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `endingType`, `grade`, `title`, `totalAssets`, `totalIncome` 외 4 |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "endingType": "CLEAR",
  "grade": "S",
  "title": "부동산 갑부",
  "totalAssets": 500000000,
  "totalIncome": 120000000,
  "totalExpense": 80000000,
  "netProfit": 40000000,
  "spendingPattern": {
    "topCategory": "식비",
    "topCategoryRatio": 35.2
  },
  "achievements": [
    { "name": "첫 내집 마련", "iconUrl": "/images/badges/first-house.png" },
    { "name": "주식 부자", "iconUrl": "/images/badges/stock-rich.png" }
  ]
}
```

</details>

> `endingType`: `CLEAR` | `BANKRUPT` | `TIMEOUT` | `FORECLOSURE`

#### `GET /games/sessions/{id}/logs` — 세션 히스토리 시계열 데이터

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/games/sessions/{id}/logs` |
| Path Params | `id` |
| Query Params | 없음 |
| Request | 없음 |
| Success | `200` |
| Response | `timeline` |

<details>
<summary>Response `200` JSON</summary>

```json
{
  "timeline": [
    {
      "turnNumber": 1,
      "date": "2026-01-01",
      "cash": 13000000,
      "netAssets": 13000000,
      "totalAssets": 13000000,
      "stockValue": 0,
      "loanBalance": 0,
      "salary": 2000000
    },
    {
      "turnNumber": 12,
      "date": "2026-12-01",
      "cash": 5000000,
      "netAssets": 25000000,
      "totalAssets": 75000000,
      "stockValue": 5000000,
      "loanBalance": 50000000,
      "salary": 2200000
    }
  ]
}
```

</details>

---

## 8. 공통 에러 응답

모든 API에서 에러 발생 시 아래 형식으로 반환:

```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "code": "INVALID_SLOT_ACTION",
  "message": "유효하지 않은 행동입니다.",
  "timestamp": "2026-03-11T14:00:00"
}
```

| HTTP Status | 주요 에러 코드 | 설명 |
|-------------|-------------|------|
| `400` | `INVALID_*` | 잘못된 요청 파라미터 |
| `401` | `UNAUTHORIZED` | 인증 실패 / 토큰 만료 |
| `403` | `FORBIDDEN` | 접근 권한 없음 |
| `404` | `NOT_FOUND` | 리소스 없음 |
| `409` | `CONFLICT` | 중복 (이미 구독 등) |
| `422` | `INSUFFICIENT_CASH` | 잔액 부족 |
| `422` | `MAX_CARDS_EXCEEDED` | 카드 최대 2장 초과 |
| `422` | `MAX_SESSIONS_EXCEEDED` | 세션 최대 3개 초과 |

---
*Generated by Antigravity AI based on 화면설계 + 기획 문서 (v2.2, 2026-03-12)*
