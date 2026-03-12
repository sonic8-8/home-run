# 🗄️ Repository 설계 원칙

## 개요

Repository는 **도메인과 데이터 소스 사이의 계약**이다.  
- `domain/repositories/` → **인터페이스** (I 접두사, 무엇을 할 수 있는지)  
- `data/repositories/` → **구현체** (Impl 접미사, 어떻게 하는지)  

UseCase는 인터페이스만 알고, 구현체는 DI로 주입된다.

---

## 파일 위치

```
features/{feature}/
├── domain/
│   └── repositories/
│       └── IAuthRepository.ts       # 인터페이스만
└── data/
    └── repositories/
        └── AuthRepositoryImpl.ts    # 구현체
```

---

## Repository 인터페이스 (domain 계층)

### 작성 규칙
- `I` 접두사 필수
- 메서드는 **동사 + 명사** 형태
- 반환 타입은 반드시 **Entity** (Model/DTO 반환 금지)
- 구현 세부사항 노출 금지 (HTTP, localStorage 언급 금지)

```ts
// features/auth/domain/repositories/IAuthRepository.ts
import { User } from '../entities/User';
import { LoginCredentials } from '../entities/LoginCredentials';

export interface IAuthRepository {
  login(credentials: LoginCredentials): Promise<User>;
  logout(): Promise<void>;
  getMe(): Promise<User>;
  refreshToken(): Promise<void>;
}
```

```ts
// features/loan/domain/repositories/ILoanRepository.ts
import { Loan } from '../entities/Loan';
import { LoanRequest } from '../entities/LoanRequest';

export interface ILoanRepository {
  applyLoan(request: LoanRequest): Promise<Loan>;
  getLoanById(id: string): Promise<Loan | null>;
  getLoansByUser(userId: string): Promise<Loan[]>;
  cancelLoan(id: string): Promise<void>;
}
```

```ts
// features/game/domain/repositories/IGameRepository.ts
import { Character } from '../entities/Character';
import { Scene } from '../entities/Scene';
import { Position } from '../entities/Position';

export interface IGameRepository {
  getCharacter(id: string): Promise<Character>;
  updateCharacterPosition(id: string, position: Position): Promise<Character>;
  getCurrentScene(): Promise<Scene>;
  saveGameState(character: Character): Promise<void>;
}
```

---

## 기본 Repository 인터페이스 (선택적 상속)

CRUD가 반복되는 경우 Base 인터페이스로 추상화한다.

```ts
// shared/repositories/IBaseRepository.ts
export interface IBaseRepository<T, ID = string> {
  findById(id: ID): Promise<T | null>;
  findAll(): Promise<T[]>;
  save(entity: T): Promise<T>;
  delete(id: ID): Promise<void>;
}

// 확장
export interface ILoanRepository extends IBaseRepository<Loan> {
  applyLoan(request: LoanRequest): Promise<Loan>;
  getLoansByUser(userId: string): Promise<Loan[]>;
}
```

---

## Repository 구현체 (data 계층)

### 작성 규칙
- `Impl` 접미사 필수
- `@injectable()` 데코레이터 필수
- DataSource를 생성자에서 `@inject()`로 주입받는다
- **Model → Entity 변환**은 구현체 책임

```ts
// features/auth/data/repositories/AuthRepositoryImpl.ts
import { injectable, inject } from 'tsyringe';
import { IAuthRepository } from '../../domain/repositories/IAuthRepository';
import { AuthRemoteDataSource } from '../datasources/AuthRemoteDataSource';
import { User } from '../../domain/entities/User';
import { LoginCredentials } from '../../domain/entities/LoginCredentials';

@injectable()
export class AuthRepositoryImpl implements IAuthRepository {
  constructor(
    @inject(AuthRemoteDataSource)
    private readonly dataSource: AuthRemoteDataSource
  ) {}

  async login(credentials: LoginCredentials): Promise<User> {
    const model = await this.dataSource.login(credentials);

    // ✅ Model → Entity 변환 (이 계층의 책임)
    return {
      id: model.user_id,
      name: model.user_name,
      email: model.email_address,
      creditScore: model.credit_score,
      createdAt: new Date(model.created_at),
    };
  }

  async logout(): Promise<void> {
    await this.dataSource.logout();
  }

  async getMe(): Promise<User> {
    const model = await this.dataSource.getMe();
    return this.toEntity(model);    // 변환 로직 메서드 분리 권장
  }

  async refreshToken(): Promise<void> {
    await this.dataSource.refreshToken();
  }

  // ✅ Model → Entity 변환 메서드 분리
  private toEntity(model: UserModel): User {
    return {
      id: model.user_id,
      name: model.user_name,
      email: model.email_address,
      creditScore: model.credit_score,
      createdAt: new Date(model.created_at),
    };
  }
}
```

---

## 게임(로컬) Repository 구현체

```ts
// features/game/data/repositories/GameRepositoryImpl.ts
import { injectable, inject } from 'tsyringe';
import { IGameRepository } from '../../domain/repositories/IGameRepository';
import { GameLocalDataSource } from '../datasources/GameLocalDataSource';
import { Character } from '../../domain/entities/Character';
import { Scene } from '../../domain/entities/Scene';
import { Position } from '../../domain/entities/Position';

@injectable()
export class GameRepositoryImpl implements IGameRepository {
  constructor(
    @inject(GameLocalDataSource)
    private readonly dataSource: GameLocalDataSource
  ) {}

  async getCharacter(id: string): Promise<Character> {
    const model = await this.dataSource.loadCharacter(id);
    return {
      id: model.id,
      name: model.name,
      position: { x: model.pos_x, y: model.pos_y },
      state: model.state as CharacterState,
      spriteKey: model.sprite_key,
    };
  }

  async updateCharacterPosition(id: string, position: Position): Promise<Character> {
    const model = await this.dataSource.saveCharacterPosition(id, position);
    return this.toCharacterEntity(model);
  }

  async getCurrentScene(): Promise<Scene> {
    const model = await this.dataSource.loadCurrentScene();
    return {
      id: model.scene_id,
      name: model.scene_name,
      mapData: model.map_data,
    };
  }

  async saveGameState(character: Character): Promise<void> {
    await this.dataSource.saveGameState({
      id: character.id,
      pos_x: character.position.x,
      pos_y: character.position.y,
      state: character.state,
      sprite_key: character.spriteKey,
    });
  }

  private toCharacterEntity(model: CharacterModel): Character {
    return {
      id: model.id,
      name: model.name,
      position: { x: model.pos_x, y: model.pos_y },
      state: model.state as CharacterState,
      spriteKey: model.sprite_key,
    };
  }
}
```

---

## 메서드 네이밍 가이드

| 동작 | 메서드명 패턴 | 예시 |
|------|------------|------|
| 단건 조회 | `get{Entity}By{Key}` | `getLoanById`, `getUserByEmail` |
| 목록 조회 | `get{Entity}s`, `getAll{Entity}s` | `getLoans`, `getAllProperties` |
| 생성/저장 | `save`, `create{Entity}`, `apply{Action}` | `saveLoan`, `applyLoan` |
| 수정 | `update{Entity}`, `update{Field}` | `updateCharacterPosition` |
| 삭제 | `delete{Entity}`, `cancel{Entity}` | `deleteLoan`, `cancelLoan` |
| 존재 확인 | `exists{Entity}` | `existsUser` |

---

## 금지 패턴

```ts
// ❌ 인터페이스에서 구현 세부사항 노출
export interface IAuthRepository {
  login(creds: LoginCredentials): Promise<AxiosResponse<UserModel>>;  // Model 반환 금지
  getLocalStorage(key: string): string;                                // 기술 세부사항 금지
}

// ❌ 구현체에서 인터페이스 없이 직접 사용
// UseCase가 AuthRepositoryImpl을 직접 주입받음 - 금지
@inject(AuthRepositoryImpl)
private readonly authRepository: AuthRepositoryImpl;

// ✅ 반드시 인터페이스 타입으로 주입
@inject('IAuthRepository')
private readonly authRepository: IAuthRepository;

// ❌ 구현체에서 비즈니스 로직 처리
export class LoanRepositoryImpl implements ILoanRepository {
  async applyLoan(request: LoanRequest): Promise<Loan> {
    if (request.amount > 100_000_000) {    // 도메인 규칙은 UseCase 책임
      throw new Error('한도 초과');
    }
    ...
  }
}
```
