# 🌐 core/network 설계 원칙

## 이게 왜 필요한가?

모든 API 호출마다 아래 코드를 반복하고 싶지 않다면:

```ts
// ❌ 이런 코드가 DataSource마다 반복되면 안 된다
const res = await axios.get('https://api.example.com/loan', {
  headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
  timeout: 5000,
});
if (res.status === 401) { window.location.href = '/login'; }
```

`core/network/`는 이 반복을 **한 곳에서 처리**한다.  
DataSource는 그냥 `apiClient.get('/loan')` 만 호출하면 된다.

---

## 위치

```
core/network/
├── apiClient.ts       # Axios 인스턴스 (baseURL, timeout 설정)
└── interceptors.ts    # 요청/응답 공통 처리 (토큰 자동 첨부, 에러 변환)
```

---

## apiClient.ts

모든 API 요청의 **기본 설정**을 담는 Axios 인스턴스다.

```ts
// core/network/apiClient.ts
import axios from 'axios';
import { setupInterceptors } from './interceptors';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,  // 환경 변수로 URL 관리
  timeout: 10_000,                              // 10초 초과 시 자동 실패
  headers: {
    'Content-Type': 'application/json',
  },
});

// 인터셉터 연결
setupInterceptors(apiClient);

export { apiClient };
```

### 환경 변수 파일

```
# .env.development
VITE_API_BASE_URL=http://localhost:8080/api

# .env.production
VITE_API_BASE_URL=https://api.mygame.com/api
```

---

## interceptors.ts

인터셉터는 **모든 요청/응답을 가로채서 공통 처리**를 하는 미들웨어다.

### 처리하는 것들
| 상황 | 처리 내용 |
|------|----------|
| 요청 보내기 전 | 토큰 자동 첨부 |
| 응답 성공 | 그대로 통과 |
| 응답 401 | 토큰 만료 → 갱신 시도 → 재요청 |
| 응답 403 | 권한 없음 오류 |
| 응답 500+ | 서버 오류 변환 |
| 네트워크 끊김 | 네트워크 오류 변환 |

```ts
// core/network/interceptors.ts
import { AxiosInstance, AxiosError } from 'axios';
import { NetworkError, UnauthorizedError } from '@core/error/AppError';

export const setupInterceptors = (client: AxiosInstance): void => {

  // ── 요청 인터셉터 ──────────────────────────────────────────────────
  client.interceptors.request.use(
    (config) => {
      // 토큰이 있으면 모든 요청에 자동 첨부
      const token = localStorage.getItem('accessToken');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => Promise.reject(error)
  );

  // ── 응답 인터셉터 ──────────────────────────────────────────────────
  client.interceptors.response.use(
    // 성공 응답: 그대로 통과
    (response) => response,

    // 에러 응답: 공통 에러 타입으로 변환
    async (error: AxiosError) => {
      const status = error.response?.status;

      // 401: 토큰 만료 → 갱신 시도
      if (status === 401) {
        try {
          const refreshToken = localStorage.getItem('refreshToken');
          if (!refreshToken) throw new Error('리프레시 토큰 없음');

          const { data } = await client.post('/auth/refresh', { refreshToken });
          localStorage.setItem('accessToken', data.accessToken);

          // 원래 요청 재시도
          if (error.config) {
            error.config.headers.Authorization = `Bearer ${data.accessToken}`;
            return client.request(error.config);
          }
        } catch {
          // 갱신 실패 → 로그아웃 처리
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          window.location.href = '/login';
        }
      }

      // 403: 권한 없음
      if (status === 403) {
        return Promise.reject(
          new NetworkError('접근 권한이 없습니다.', 403)
        );
      }

      // 500+: 서버 오류
      if (status && status >= 500) {
        return Promise.reject(
          new NetworkError('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.', status)
        );
      }

      // 네트워크 연결 없음
      if (!error.response) {
        return Promise.reject(
          new NetworkError('인터넷 연결을 확인해주세요.', 0)
        );
      }

      // 나머지: 원본 에러 그대로 전달
      return Promise.reject(error);
    }
  );
};
```

---

## DataSource에서 apiClient 사용법

```ts
// features/loan/data/datasources/LoanRemoteDataSource.ts
import { injectable } from 'tsyringe';
import { apiClient } from '@core/network/apiClient';  // ← 이것만 import
import { LoanModel, LoanApplyRequestModel } from '../models/LoanModel';

@injectable()
export class LoanRemoteDataSource {
  // ✅ 그냥 경로만 입력하면 됨 (baseURL, 토큰은 자동)
  async getLoans(): Promise<LoanModel[]> {
    const { data } = await apiClient.get<LoanModel[]>('/loans');
    return data;
  }

  async applyLoan(request: LoanApplyRequestModel): Promise<LoanModel> {
    const { data } = await apiClient.post<LoanModel>('/loans', request);
    return data;
  }

  async getLoanById(id: string): Promise<LoanModel> {
    const { data } = await apiClient.get<LoanModel>(`/loans/${id}`);
    return data;
  }

  async cancelLoan(id: string): Promise<void> {
    await apiClient.delete(`/loans/${id}`);
  }
}
```

---

## 인증이 필요 없는 요청

특정 요청은 토큰 없이 보내야 할 때:

```ts
// 로그인 요청 - 토큰 없이 보내야 함
async login(credentials: LoginRequestModel): Promise<LoginResponseModel> {
  const { data } = await apiClient.post<LoginResponseModel>(
    '/auth/login',
    credentials,
    {
      headers: { Authorization: undefined }  // 이 요청만 토큰 제거
    }
  );
  return data;
}
```

---

## 금지 패턴

```ts
// ❌ DataSource에서 axios 직접 import
import axios from 'axios';  // apiClient 사용할 것

// ❌ DataSource마다 baseURL 하드코딩
const res = await axios.get('http://localhost:8080/api/loan');

// ❌ 토큰을 DataSource마다 수동으로 붙이기
headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
// interceptors에서 자동 처리
```
