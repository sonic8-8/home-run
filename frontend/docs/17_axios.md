# 📡 Axios 사용 원칙

## Axios란?

브라우저에서 서버로 HTTP 요청을 보내는 라이브러리다.  
기본 내장된 `fetch`보다 **자동 JSON 변환, 인터셉터, 에러 처리**가 편리해서 사용한다.

```
내가 작성하는 코드           Axios가 해주는 것
─────────────────           ──────────────────────────────────
apiClient.get('/loans')  →  HTTP GET 요청 전송
                         ←  JSON 응답을 자동으로 객체로 변환
                             에러 발생 시 throw (직접 체크 불필요)
```

---

## 설치

```bash
npm install axios
```

---

## 기본 사용 원칙

이 프로젝트에서 Axios는 **절대 직접 import하지 않는다.**  
반드시 `core/network/apiClient.ts`에서 만든 인스턴스를 사용한다.

```ts
// ❌ 절대 이렇게 쓰지 않는다
import axios from 'axios';
const res = await axios.get('http://localhost:8080/loans');

// ✅ 항상 이렇게 쓴다
import { apiClient } from '@core/network/apiClient';
const { data } = await apiClient.get('/loans');
```

이유: `apiClient`를 쓰면 `baseURL`, `토큰 자동 첨부`, `에러 공통 처리`가 모두 자동으로 적용된다.

---

## HTTP 메서드별 사용법

### GET — 데이터 조회

```ts
// 기본 조회
const { data } = await apiClient.get<LoanModel[]>('/loans');

// 경로 파라미터 (URL에 ID 포함)
const { data } = await apiClient.get<LoanModel>(`/loans/${loanId}`);

// 쿼리 파라미터 (?page=1&limit=10)
const { data } = await apiClient.get<LoanListResponseModel>('/loans', {
  params: {
    page: 1,
    limit: 10,
    status: 'PENDING',
  },
});
// 실제 요청: GET /loans?page=1&limit=10&status=PENDING
```

### POST — 데이터 생성 / 액션 실행

```ts
// 요청 바디 포함
const { data } = await apiClient.post<LoanModel>('/loans', {
  amount: 5_000_000,
  term: 12,
  purpose: '생활비',
});

// 응답 데이터가 없는 경우
await apiClient.post('/auth/logout');
```

### PUT — 데이터 전체 수정

```ts
// 리소스 전체 교체
const { data } = await apiClient.put<UserModel>(`/users/${userId}`, {
  name: '홍길동',
  email: 'hong@example.com',
  phone: '010-1234-5678',
});
```

### PATCH — 데이터 일부 수정

```ts
// 특정 필드만 수정
const { data } = await apiClient.patch<UserModel>(`/users/${userId}`, {
  name: '홍길동',   // 이름만 바꾸고 싶을 때
});
```

### DELETE — 데이터 삭제

```ts
// 삭제 (보통 응답 바디 없음)
await apiClient.delete(`/loans/${loanId}`);

// 삭제 후 응답 있는 경우
const { data } = await apiClient.delete<{ message: string }>(`/loans/${loanId}`);
```

---

## 응답 타입 지정 (Generic)

Axios는 제네릭으로 응답 타입을 지정할 수 있다.  
**반드시 Model 타입을 명시**해서 `data`가 자동으로 타입을 갖게 한다.

```ts
// ✅ 타입 명시 - data가 LoanModel[] 타입
const { data } = await apiClient.get<LoanModel[]>('/loans');
//                                   ^^^^^^^^^^^^
//                                   이 부분이 응답 data의 타입

// ❌ 타입 미명시 - data가 any 타입
const { data } = await apiClient.get('/loans');
```

### 응답 구조 이해

```ts
// Axios 응답 객체 구조
const response = await apiClient.get<LoanModel[]>('/loans');

response.data      // 실제 데이터 (LoanModel[])  ← 거의 항상 이것만 씀
response.status    // HTTP 상태 코드 (200, 201 등)
response.headers   // 응답 헤더

// 실무에서는 구조분해로 data만 꺼낸다
const { data } = await apiClient.get<LoanModel[]>('/loans');
```

---

## 실제 DataSource 작성 패턴

```ts
// features/loan/data/datasources/LoanRemoteDataSource.ts
import { injectable } from 'tsyringe';
import { apiClient } from '@core/network/apiClient';
import {
  LoanModel,
  LoanApplyRequestModel,
  LoanListResponseModel,
} from '../models/LoanModel';

@injectable()
export class LoanRemoteDataSource {

  // 목록 조회 (페이지네이션)
  async getLoans(page: number, limit: number): Promise<LoanListResponseModel> {
    const { data } = await apiClient.get<LoanListResponseModel>('/loans', {
      params: { page, limit },
    });
    return data;
  }

  // 단건 조회
  async getLoanById(id: string): Promise<LoanModel> {
    const { data } = await apiClient.get<LoanModel>(`/loans/${id}`);
    return data;
  }

  // 대출 신청
  async applyLoan(request: LoanApplyRequestModel): Promise<LoanModel> {
    const { data } = await apiClient.post<LoanModel>('/loans', request);
    return data;
  }

  // 대출 취소
  async cancelLoan(id: string): Promise<void> {
    await apiClient.patch(`/loans/${id}/cancel`);
  }

  // 대출 삭제
  async deleteLoan(id: string): Promise<void> {
    await apiClient.delete(`/loans/${id}`);
  }
}
```

---

## 파일 업로드

이미지나 파일을 보낼 때는 `FormData`를 사용한다.

```ts
async uploadProfileImage(userId: string, file: File): Promise<{ imageUrl: string }> {
  const formData = new FormData();
  formData.append('image', file);

  const { data } = await apiClient.post<{ imageUrl: string }>(
    `/users/${userId}/profile-image`,
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data',  // FormData 전송 시 필수
      },
    }
  );
  return data;
}
```

---

## 에러 처리

`apiClient`를 사용하면 인터셉터가 에러를 `NetworkError`로 변환해서 throw한다.  
DataSource는 에러를 try/catch하지 않고 위로 전파한다.

```ts
// ✅ DataSource는 에러를 그냥 위로 보낸다
async getLoans(): Promise<LoanModel[]> {
  const { data } = await apiClient.get<LoanModel[]>('/loans');
  // 실패하면 interceptors가 NetworkError로 변환해서 throw
  // → RepositoryImpl → UseCase → feature 훅 에서 catch
  return data;
}

// ❌ DataSource에서 에러를 직접 catch하지 않는다
async getLoans(): Promise<LoanModel[]> {
  try {
    const { data } = await apiClient.get<LoanModel[]>('/loans');
    return data;
  } catch (e) {
    console.error(e);
    return [];   // 에러를 삼키면 훅에서 처리 불가
  }
}
```

에러가 발생했을 때 최종 처리는 **feature 훅**에서 한다. (→ `15_error.md` 참고)

---

## 자주 쓰는 요청 옵션

```ts
// timeout 개별 설정 (기본 10초를 특정 요청만 다르게)
await apiClient.get('/heavy-data', { timeout: 30_000 });

// 요청 취소 (컴포넌트 언마운트 시)
const controller = new AbortController();

const { data } = await apiClient.get('/loans', {
  signal: controller.signal,
});

// 취소
controller.abort();

// 실제 사용 (useEffect에서)
useEffect(() => {
  const controller = new AbortController();

  apiClient.get('/loans', { signal: controller.signal })
    .then(({ data }) => setLoans(data))
    .catch((e) => {
      if (axios.isCancel(e)) return;  // 취소된 요청은 무시
      setError('불러오기 실패');
    });

  return () => controller.abort();  // 언마운트 시 취소
}, []);
```

---

## HTTP 상태 코드 정리

DataSource나 인터셉터 작성 시 참고한다.

| 코드 | 의미 | 처리 방법 |
|------|------|----------|
| `200` | 성공 | 데이터 사용 |
| `201` | 생성 성공 | 생성된 리소스 사용 |
| `204` | 성공 (응답 없음) | data 없음, void 처리 |
| `400` | 잘못된 요청 | 서버 에러 메시지 표시 |
| `401` | 인증 필요 | 토큰 갱신 → 재시도 |
| `403` | 권한 없음 | 접근 불가 안내 |
| `404` | 없는 리소스 | "찾을 수 없음" 안내 |
| `409` | 충돌 (중복 등) | 중복 안내 메시지 |
| `500+` | 서버 오류 | "서버 오류" Toast 표시 |

---

## 금지 패턴 요약

```ts
// ❌ axios 직접 import
import axios from 'axios';

// ❌ baseURL 하드코딩
await axios.get('http://localhost:8080/loans');

// ❌ 토큰 수동 첨부 (인터셉터가 자동 처리)
headers: { Authorization: `Bearer ${token}` }

// ❌ DataSource에서 에러 삼키기
} catch (e) { return []; }

// ❌ 응답 타입 미지정
const { data } = await apiClient.get('/loans');  // data가 any
```
