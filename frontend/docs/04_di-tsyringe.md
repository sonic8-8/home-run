# 💉 DI (Dependency Injection) 설계 원칙

## 개요

**tsyringe**를 사용하여 IoC 컨테이너 기반 의존성 주입을 구현한다.  
UseCase → Repository Interface → Repository 구현체 의 역전된 의존 구조를 유지한다.

---

## 설치 및 초기 설정

```bash
npm install tsyringe reflect-metadata
```

```ts
// src/main.tsx (앱 진입점 최상단)
import 'reflect-metadata';  // ← 반드시 첫 번째 import
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './app/App';
```

```json
// tsconfig.json
{
  "compilerOptions": {
    "experimentalDecorators": true,
    "emitDecoratorMetadata": true
  }
}
```

---

## 컨테이너 설정

```ts
// core/di/container.ts
import { container } from 'tsyringe';

// Data Sources
import { AuthRemoteDataSource } from '@features/auth/data/datasources/AuthRemoteDataSource';
import { GameLocalDataSource } from '@features/game/data/datasources/GameLocalDataSource';
import { LoanRemoteDataSource } from '@features/loan/data/datasources/LoanRemoteDataSource';

// Repository 구현체
import { AuthRepositoryImpl } from '@features/auth/data/repositories/AuthRepositoryImpl';
import { GameRepositoryImpl } from '@features/game/data/repositories/GameRepositoryImpl';
import { LoanRepositoryImpl } from '@features/loan/data/repositories/LoanRepositoryImpl';

// Repository 인터페이스 토큰 등록
container.register('IAuthRepository', { useClass: AuthRepositoryImpl });
container.register('IGameRepository', { useClass: GameRepositoryImpl });
container.register('ILoanRepository', { useClass: LoanRepositoryImpl });

// DataSource 등록 (싱글톤)
container.registerSingleton(AuthRemoteDataSource);
container.registerSingleton(GameLocalDataSource);
container.registerSingleton(LoanRemoteDataSource);

export { container };
```

---

## 등록 방식 선택 기준

| 방식 | 사용 시점 | 예시 |
|------|----------|------|
| `registerSingleton` | 앱 전체에서 단일 인스턴스 유지 | DataSource, apiClient |
| `register(...useClass)` | 인터페이스 → 구현체 매핑 | IRepository → RepositoryImpl |
| `register(...useValue)` | 값/인스턴스 직접 등록 | 설정 객체, 상수 |
| `register(...useFactory)` | 생성 로직이 복잡한 경우 | 조건부 구현체 선택 |

---

## Repository 구현체 작성

```ts
// features/auth/data/repositories/AuthRepositoryImpl.ts
import { injectable, inject } from 'tsyringe';
import { IAuthRepository } from '../../domain/repositories/IAuthRepository';
import { AuthRemoteDataSource } from '../datasources/AuthRemoteDataSource';
import { User } from '../../domain/entities/User';
import { LoginCredentials } from '../../domain/entities/LoginCredentials';
import { UserModel } from '../models/UserModel';

@injectable()
export class AuthRepositoryImpl implements IAuthRepository {
  constructor(
    @inject(AuthRemoteDataSource)
    private readonly dataSource: AuthRemoteDataSource
  ) {}

  async login(credentials: LoginCredentials): Promise<User> {
    const model: UserModel = await this.dataSource.login(credentials);
    // Model → Entity 변환 (매핑)
    return {
      id: model.user_id,
      name: model.user_name,
      email: model.email_address,
    };
  }

  async getMe(): Promise<User> {
    const model = await this.dataSource.getMe();
    return {
      id: model.user_id,
      name: model.user_name,
      email: model.email_address,
    };
  }
}
```

---

## DataSource 작성

```ts
// features/auth/data/datasources/AuthRemoteDataSource.ts
import { injectable } from 'tsyringe';
import { apiClient } from '@core/network/apiClient';
import { UserModel } from '../models/UserModel';

@injectable()
export class AuthRemoteDataSource {
  async login(credentials: { email: string; password: string }): Promise<UserModel> {
    const { data } = await apiClient.post<UserModel>('/auth/login', credentials);
    return data;
  }

  async getMe(): Promise<UserModel> {
    const { data } = await apiClient.get<UserModel>('/auth/me');
    return data;
  }
}
```

---

## UseCase 작성 (DI 적용)

```ts
// features/auth/domain/usecases/LoginUseCase.ts
import { injectable, inject } from 'tsyringe';
import { IAuthRepository } from '../repositories/IAuthRepository';
import { User } from '../entities/User';
import { LoginCredentials } from '../entities/LoginCredentials';

@injectable()
export class LoginUseCase {
  constructor(
    @inject('IAuthRepository')               // 문자열 토큰으로 인터페이스 주입
    private readonly authRepository: IAuthRepository
  ) {}

  async execute(credentials: LoginCredentials): Promise<User> {
    return this.authRepository.login(credentials);
  }
}
```

---

## Presentation 훅에서 UseCase 해결

```ts
// features/auth/presentation/hooks/useAuth.ts
import { useState } from 'react';
import { container } from '@core/di/container';
import { LoginUseCase } from '../../domain/usecases/LoginUseCase';
import { User } from '../../domain/entities/User';

export const useAuth = () => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const login = async (email: string, password: string) => {
    setIsLoading(true);
    try {
      // ✅ container.resolve() 로 UseCase 인스턴스 획득
      const loginUseCase = container.resolve(LoginUseCase);
      const result = await loginUseCase.execute({ email, password });
      setUser(result);
    } finally {
      setIsLoading(false);
    }
  };

  return { user, login, isLoading };
};
```

---

## 토큰 관리 원칙

### 인터페이스 토큰은 상수로 중앙 관리
```ts
// core/di/tokens.ts
export const DI_TOKENS = {
  IAuthRepository: 'IAuthRepository',
  ILoanRepository: 'ILoanRepository',
  IGameRepository: 'IGameRepository',
  IPropertyRepository: 'IPropertyRepository',
  ICardRepository: 'ICardRepository',
} as const;
```

```ts
// 사용 시
@inject(DI_TOKENS.IAuthRepository)
private readonly authRepository: IAuthRepository
```

---

## 테스트에서 DI 활용 (Mock 교체)

```ts
// __tests__/LoginUseCase.test.ts
import { container } from 'tsyringe';
import { LoginUseCase } from '@features/auth/domain/usecases/LoginUseCase';
import { IAuthRepository } from '@features/auth/domain/repositories/IAuthRepository';

describe('LoginUseCase', () => {
  beforeEach(() => {
    // Mock Repository 등록
    const mockAuthRepository: IAuthRepository = {
      login: jest.fn().mockResolvedValue({ id: '1', name: 'Test', email: 'test@test.com' }),
      logout: jest.fn(),
      getMe: jest.fn(),
    };
    container.register('IAuthRepository', { useValue: mockAuthRepository });
  });

  afterEach(() => {
    container.clearInstances();
  });

  it('올바른 자격증명으로 로그인 성공', async () => {
    const useCase = container.resolve(LoginUseCase);
    const result = await useCase.execute({ email: 'test@test.com', password: '1234' });
    expect(result.id).toBe('1');
  });
});
```

---

## 의존성 등록 순서

```
main.tsx
  └─ import 'reflect-metadata'       # 1. 메타데이터 활성화
  └─ import App
       └─ app/App.tsx
            └─ core/di/container.ts  # 2. 컨테이너 등록 (앱 시작 전)
            └─ 실제 앱 렌더링         # 3. 이후 container.resolve() 사용 가능
```

---

## 금지 패턴

```ts
// ❌ UseCase 내부에서 직접 구현체 생성
export class LoginUseCase {
  private repo = new AuthRepositoryImpl();   // DI 원칙 위반
}

// ❌ Presentation에서 Repository 직접 접근
const repo = container.resolve(AuthRepositoryImpl);   // UseCase를 거쳐야 함
repo.login(credentials);

// ❌ @injectable() 누락
export class LoginUseCase {    // 데코레이터 없으면 tsyringe가 인식 못함
  constructor(private repo: IAuthRepository) {}
}
```
