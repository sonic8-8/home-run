# 홈런 (Home-Run) API 명세서

> **Base URL**: `/api`
> **기준 문서**: `01_homerun_game_design.md` v2.3, `02_homerun_requirements.md` v2.2
> **버전**: v2.0 (2026-03-11 — 화면설계 반영, JSON req/res 추가)

---

## 1. Auth & User (인증 및 유저)

### 1.1 소셜 로그인

#### `POST /auth/login/kakao` — 카카오 로그인

```json
// Request
{
  "authorizationCode": "카카오_인가코드"
}

// Response 200
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "isNewUser": true
}
```

#### `POST /auth/login/ssafy` — SSAFY 로그인

```json
// Request
{
  "authorizationCode": "SSAFY_인가코드"
}

// Response 200
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "isNewUser": false
}
```

### 1.2 이메일 인증

#### `POST /auth/signup` — 이메일 회원가입

```json
// Request
{
  "name": "김싸피",
  "email": "example@examl.com",
  "password": "P@ssw0rd!",
  "passwordConfirm": "P@ssw0rd!",
  "termsAgreed": true
}

// Response 201
{
  "userId": 1,
  "email": "example@examl.com",
  "name": "김싸피"
}
```

#### `POST /auth/login` — 이메일 로그인

```json
// Request
{
  "email": "example@examl.com",
  "password": "P@ssw0rd!"
}

// Response 200
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

#### `POST /auth/refresh` — 토큰 갱신

```json
// Request Header
Authorization: Bearer {refreshToken}

// Response 200
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

### 1.3 유저 프로필

#### `PATCH /users/me/nickname` — 닉네임 설정

```json
// Request
{
  "nickname": "김싸피"
}

// Response 200
{
  "userId": 1,
  "nickname": "김싸피"
}
```

#### `POST /users/me/ssafy-connect` — SSAFY 금융 연동 (마이데이터)

```json
// Request
{
  "userKey": "ssafy-user-key-xxx",
  "apiKey": "ssafy-api-key-xxx"
}

// Response 200
{
  "connected": true,
  "connectedAt": "2026-03-11T14:00:00"
}
```

---

## 2. Part A: Financial Home (금융 홈)

### 2.1 대시보드

#### `GET /home/dashboard` — 금융 홈 요약 데이터

```json
// Response 200
{
  "totalAssets": 42300000,
  "monthlyIncome": 2800000,
  "monthlyExpense": 1420000,
  "incomeChangeFromLastMonth": 0,
  "expenseChangeFromLastMonth": 220000,
  "nextPaydayDays": 7
}
```

#### `GET /home/spending` — 카테고리별 소비 분석

```json
// Request Query
?month=2026-03

// Response 200
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

### 2.2 시드머니 저축 계좌

#### `GET /seedmoney/account` — 시드머니 계좌 조회

```json
// Response 200
{
  "bankName": "싸피은행",
  "accountNumber": "110-123-000000",
  "balance": 300000
}
```

#### `POST /seedmoney/transfer` — 송금

```json
// Request
{
  "toAccountNumber": "110-456-000000",
  "amount": 50000
}

// Response 200
{
  "transactionId": "TXN-20260311-001",
  "remainingBalance": 250000
}
```

#### `POST /seedmoney/deposit` — 입금

```json
// Request
{
  "fromAccountNumber": "110-789-000000",
  "amount": 100000
}

// Response 200
{
  "transactionId": "TXN-20260311-002",
  "remainingBalance": 400000
}
```

### 2.3 소비통제 PASS 시스템

#### `GET /pass/products` — 전체 PASS 상품 목록 조회

```json
// Response 200
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

#### `GET /pass/subscriptions` — 내 PASS 구독 목록 조회

```json
// Response 200
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

> `weeklyHistory`: 월~일 순서, 해당 요일 저축 여부

#### `POST /pass/subscribe` — PASS 구독 신청

```json
// Request
{
  "passId": 3,
  "sourceAccountId": "110-123-000000"
}

// Response 201
{
  "subscriptionId": 3,
  "passId": 3,
  "name": "택시 PASS",
  "subscribedAt": "2026-03-11T14:00:00"
}
```

#### `DELETE /pass/subscriptions/{subscriptionId}` — PASS 구독 해지

```json
// Response 204 No Content
```

#### `POST /pass/save` — 꾹 눌러 저축 (즉시 이체)

```json
// Request
{
  "subscriptionId": 1,
  "sourceAccountId": "110-123-000000"
}

// Response 200
{
  "savedAmount": 5000,
  "totalSaved": 70000,
  "remainingBalance": 295000
}
```

#### `GET /pass/widget` — 오늘 절약 현황 위젯

```json
// Response 200
{
  "todaySaved": 45000,
  "weeklySaved": 182000,
  "weeklyGoal": 500000,
  "progressRate": 36,
  "remaining": 318000
}
```

#### `GET /pass/history` — 저축 내역 조회

```json
// Request Query
?page=0&size=10

// Response 200
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

### 2.4 신용등급

#### `GET /credit/score` — 신용등급 조회

```json
// Response 200
{
  "kcbScore": 936,
  "niceScore": 948,
  "baseScore": 936,
  "estimatedMinRate": 4.89
}
```

### 2.5 금융 홈 추천

#### `GET /home/loan-recommendations` — 대출 상품 추천

```json
// Response 200
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

#### `GET /home/card-recommendations` — 카드 상품 추천

```json
// Response 200
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

---

## 3. Part B: Game Core (게임 핵심)

### 3.1 세션 관리 (최대 3슬롯)

#### `GET /games/sessions` — 내 세션 목록 조회

```json
// Response 200
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

#### `POST /games/sessions` — 새 게임 세션 생성

```json
// Request
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

// Response 201
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

#### `GET /games/sessions/{id}` — 특정 세션 로드 (이어하기)

```json
// Response 200
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

#### `DELETE /games/sessions/{id}` — 세션 삭제

```json
// Response 204 No Content
```

### 3.2 게임 초기화 (선택지 조회)

#### `GET /games/characters` — 캐릭터 목록 조회

```json
// Response 200
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

#### `GET /games/job-types` — 직업 유형 목록 조회

```json
// Response 200
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

> `stats` 값은 0~100 게이지 표시용

#### `GET /games/regions` — 목표 지역 (시/도) 목록 조회

```json
// Response 200
{
  "regions": [
    { "regionCode": "SEOUL", "name": "서울" },
    { "regionCode": "GWANGJU", "name": "광주" }
  ]
}
```

#### `GET /games/regions/{regionCode}/districts` — 구/군 목록 조회

```json
// Response 200
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

#### `GET /games/regions/{regionCode}/districts/{districtCode}/properties` — 매물 목록 조회

```json
// Response 200
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

#### `POST /games/sessions/{id}/init-capital` — 초기 자본 세팅 (마이데이터 연동)

```json
// Request
{
  "useMyData": true
}

// Response 200
{
  "initialCash": 13000000,
  "monthlySalary": 2000000,
  "initialLoan": 0,
  "dataSource": "MY_DATA"
}
```

---

## 4. Part B: Game Cycle (턴 진행)

### 4.1 턴 흐름

#### `GET /games/sessions/{id}/turn` — 현재 턴 상태/뉴스 조회

```json
// Response 200
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

### 4.2 한달 스케줄 설정

#### `GET /games/sessions/{id}/turn/actions` — 선택 가능 행동 목록 조회

```json
// Response 200
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

> 카테고리별(쇼핑/활동) 3개씩 선택, 좌우 스크롤로 추가 행동 탐색

#### `POST /games/sessions/{id}/turn/slots` — 슬롯 행동 3개 일괄 제출

```json
// Request
{
  "slots": [
    { "slotIndex": 0, "actionType": "COFFEE" },
    { "slotIndex": 1, "actionType": "STUDY" },
    { "slotIndex": 2, "actionType": "EXERCISE" }
  ]
}

// Response 200
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

#### `POST /games/sessions/{id}/turn/commit` — 턴 종료 및 정산

```json
// Response 200
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

---

## 5. Part B: Financial Activities (게임 내 금융 활동)

### 5.1 내 자산 상세

#### `GET /games/sessions/{id}/assets` — 내 자산 종합 조회 (내자산 탭)

```json
// Response 200
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

#### `GET /games/sessions/{id}/news/history` — 지난 뉴스 조회 (지난뉴스 탭)

```json
// Response 200
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

### 5.2 주식 거래 (`/games/sessions/{id}/stocks`)

#### `GET .../stocks/market` — 주식 시장 현재가 목록

```json
// Response 200
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

#### `GET .../stocks/holdings` — 내 보유 주식 현황

```json
// Response 200
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

#### `POST .../stocks/orders` — 매수/매도 주문

```json
// Request
{
  "stockCode": "BIO",
  "orderType": "BUY",
  "quantity": 5
}

// Response 200
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

### 5.3 대출 (`/games/sessions/{id}/loans`)

#### `GET .../loans/products` — 대출 상품 목록 조회

```json
// Request Query
?category=ALL&page=0&size=10
// category: ALL | CREDIT | MORTGAGE | RECOMMENDED

// Response 200
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

#### `GET .../loans/products/{productId}` — 대출 상품 상세 조회

```json
// Response 200
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

#### `POST .../loans/calculate` — 이자 계산기

```json
// Request
{
  "repaymentMethod": "EQUAL_PRINCIPAL_INTEREST",
  "termMonths": 360,
  "principal": 200000000,
  "annualRate": 3.49
}
// repaymentMethod: EQUAL_PRINCIPAL_INTEREST | EQUAL_PRINCIPAL | BULLET

// Response 200
{
  "monthlyPayment": 897000,
  "totalInterest": 122920000,
  "totalPayment": 322920000
}
```

#### `POST .../loans/apply` — 대출 심사 신청 (Step 1→2: 매물 선택 → 심사)

```json
// Request
{
  "productId": "LOAN-NH-001",
  "propertyId": "PROP-HN-001"
}

// Response 200
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

> 심사 거부 시 `status: "REJECTED"`, `result.rejectionReason` 포함

#### `POST .../loans/confirm` — 대출 최종 신청 (Step 2→3: 금액 입력 → 계약)

```json
// Request
{
  "applicationId": "APP-001",
  "requestedAmount": 150000000,
  "agreed": true
}

// Response 200
{
  "loanId": "LOAN-ACT-001",
  "amount": 150000000,
  "annualRate": 3.49,
  "monthlyPayment": 672000,
  "contractDate": "2026-03-10",
  "status": "ACTIVE"
}
```

#### `POST .../loans/repay` — 중도 일시 상환

```json
// Request
{
  "loanId": "LOAN-ACT-001",
  "amount": 50000000
}

// Response 200
{
  "loanId": "LOAN-ACT-001",
  "repaidAmount": 50000000,
  "remainingPrincipal": 100000000,
  "updatedMonthlyPayment": 448000
}
```

### 5.4 신용카드 (`/games/sessions/{id}/cards`)

#### `GET .../cards/available` — 카드 상품 목록 조회

```json
// Request Query
?category=ALL&page=0&size=8
// category: ALL | TRAVEL | GAS | LIVING | RECOMMENDED

// Response 200
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

#### `GET .../cards` — 내 보유 카드 조회

```json
// Response 200
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

#### `POST .../cards` — 카드 등록 (최대 2장)

```json
// Request
{
  "cardId": "CARD-SS-001"
}

// Response 201
{
  "userCardId": 1,
  "cardId": "CARD-SS-001",
  "cardName": "스타벅스 삼성카드",
  "registeredAt": "2026-01-15"
}
```

#### `DELETE .../cards/{userCardId}` — 카드 해지

```json
// Response 204 No Content
```

### 5.5 부동산 및 계약 (`/games/sessions/{id}/real-estate`)

#### `GET .../real-estate/properties` — 부동산 매물 목록 (지도 표시용)

```json
// Request Query
?bounds=37.51,127.04,37.53,127.06

// Response 200
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

#### `GET .../real-estate/properties/{propertyId}` — 매물 상세 조회

```json
// Response 200
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

#### `POST .../real-estate/properties/{propertyId}/purchase` — 매물 구매

```json
// Request
{
  "loanId": "LOAN-ACT-001"
}

// Response 200
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

#### `GET .../real-estate/properties/{propertyId}/documents` — 서류 검토용 문건 조회

```json
// Response 200
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

#### `POST .../real-estate/properties/{propertyId}/contract` — 서류 검토 완료 후 계약

```json
// Request
{
  "checkedTraps": ["TRAP-01"]
}

// Response 200
{
  "success": true,
  "trapsDetected": 1,
  "trapsCorrectlyIdentified": 1,
  "contractResult": "SAFE",
  "message": "서류 검토를 통과했습니다."
}
```

#### `DELETE .../real-estate/contract` — 현재 주거 매도/퇴거

```json
// Response 200
{
  "refundedDeposit": 50000000,
  "updatedCash": 51000000,
  "previousHousingType": "JEONSE_APT",
  "currentHousingType": "NONE"
}
```

---

## 6. Part B: Events & Career (이벤트 및 직업)

### 6.1 커리어 (`/games/sessions/{id}/career`)

#### `POST .../career/negotiate` — 연봉 협상 시도 (연 1회)

```json
// Response 200
{
  "success": true,
  "previousSalary": 36000000,
  "newSalary": 39600000,
  "raiseRate": 10.0,
  "message": "연봉 협상에 성공했습니다!"
}
```

#### `GET .../career/job-offers` — 이직 가능 풀 조회

```json
// Response 200
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

#### `POST .../career/transfer` — 이직 수락

```json
// Request
{
  "offerId": "OFFER-001"
}

// Response 200
{
  "previousJobType": "STARTUP",
  "newJobType": "LARGE_BIZ",
  "newJobTitle": "대기업 직장인",
  "newSalary": 60000000,
  "tenureReset": true,
  "message": "OO 대기업으로 이직했습니다."
}
```

### 6.2 이벤트 (`/games/sessions/{id}/events`)

#### `GET .../events/pending` — 대기 중인 이벤트 조회

```json
// Response 200
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

> `type` 종류: `GIFT` (수령만), `CHOICE` (2지선다), `JOB_TRANSFER` (이직 오퍼), `PHONE` (보이스피싱 등), `LETTER` (편지/알림)

#### `POST .../events/{eventId}/resolve` — 이벤트 선택/수령 제출

```json
// Request
{
  "choiceId": "A"
}
// choiceId가 없는 이벤트(GIFT, LETTER): { "choiceId": null }

// Response 200
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

---

## 7. Part B: Ending & Report (엔딩)

#### `GET /games/sessions/{id}/ending` — 엔딩 리포트 데이터 조회

```json
// Response 200
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

> `endingType`: `CLEAR` | `BANKRUPT` | `TIMEOUT` | `FORECLOSURE`

#### `GET /games/sessions/{id}/logs` — 세션 히스토리 시계열 데이터

```json
// Response 200
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
*Generated by Antigravity AI based on 화면설계 + 기획 문서 (v2.0, 2026-03-11)*
