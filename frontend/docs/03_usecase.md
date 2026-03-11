# ⚙️ UseCase 설계 원칙

## 개요

UseCase는 **비즈니스 규칙을 캡슐화하는 단위**다.  
UI도, 데이터 소스도 아닌 순수한 "무엇을 해야 하는가"를 담는다.

---

## 핵심 원칙

1. **단일 책임** : 하나의 UseCase는 하나의 비즈니스 액션만 수행한다
2. **단일 진입점** : 반드시 `execute()` 메서드 하나로만 호출된다
3. **순수 도메인** : UI 프레임워크, HTTP, LocalStorage에 직접 의존하지 않는다
4. **DI로 주입** : Repository는 생성자에서 인터페이스 타입으로 주입받는다

---

## UseCase 기본 구조

```ts
import { injectable, inject } from 'tsyringe';
import { IAuthRepository } from '../repositories/IAuthRepository';
import { User } from '../entities/User';
import { LoginCredentials } from '../entities/LoginCredentials';

@injectable()
export class LoginUseCase {
  constructor(
    @inject('IAuthRepository')
    private readonly authRepository: IAuthRepository
  ) {}

  async execute(credentials: LoginCredentials): Promise<User> {
    // 1. 입력 유효성 검증 (도메인 규칙)
    if (!credentials.email || !credentials.password) {
      throw new Error('이메일과 비밀번호를 입력해주세요.');
    }

    // 2. 비즈니스 로직 수행
    const user = await this.authRepository.login(credentials);

    // 3. 결과 반환 (Entity)
    return user;
  }
}
```

---

## execute() 시그니처 패턴

### 입력이 단순한 경우 - 직접 파라미터
```ts
async execute(email: string, password: string): Promise<User>
```

### 입력이 복잡한 경우 - Input 타입 객체
```ts
// domain/usecases/ApplyLoanUseCase.ts
export interface ApplyLoanInput {
  amount: number;
  term: number;        // 개월
  purpose: string;
}

@injectable()
export class ApplyLoanUseCase {
  constructor(
    @inject('ILoanRepository')
    private readonly loanRepository: ILoanRepository
  ) {}

  async execute(input: ApplyLoanInput): Promise<Loan> {
    // 도메인 규칙: 대출 한도 검증
    if (input.amount > MAX_LOAN_AMOUNT) {
      throw new DomainError(`최대 대출 금액은 ${MAX_LOAN_AMOUNT}원입니다.`);
    }

    if (input.term < 6 || input.term > 60) {
      throw new DomainError('대출 기간은 6개월 ~ 60개월 사이여야 합니다.');
    }

    return this.loanRepository.applyLoan(input);
  }
}
```

### 반환값이 없는 경우
```ts
async execute(): Promise<void>
```

---

## UseCase 내부에서 허용되는 것

```ts
@injectable()
export class MoveCharacterUseCase {
  constructor(
    @inject('IGameRepository')
    private readonly gameRepository: IGameRepository
  ) {}

  async execute(characterId: string, direction: Direction): Promise<Character> {
    // ✅ 허용: 도메인 규칙 검증
    const character = await this.gameRepository.getCharacter(characterId);
    if (character.state === CharacterState.Stunned) {
      throw new DomainError('기절 상태에서는 이동할 수 없습니다.');
    }

    // ✅ 허용: 다른 Repository 메서드 호출
    const map = await this.gameRepository.getCurrentMap();

    // ✅ 허용: 순수 계산 로직
    const newPosition = calculateNewPosition(character.position, direction, map);

    // ✅ 허용: Repository를 통한 저장
    return this.gameRepository.updateCharacterPosition(characterId, newPosition);
  }
}
```

## UseCase 내부에서 금지되는 것

```ts
@injectable()
export class BadUseCase {
  async execute(): Promise<void> {
    // ❌ 금지: axios 직접 호출
    const res = await axios.get('/api/user');

    // ❌ 금지: localStorage 직접 접근
    localStorage.setItem('token', 'xxx');

    // ❌ 금지: React 훅 사용
    const [state, setState] = useState(null);

    // ❌ 금지: UI 관련 로직
    document.getElementById('modal').style.display = 'block';

    // ❌ 금지: 다른 feature의 UseCase 직접 호출
    const otherUseCase = new OtherFeatureUseCase();
  }
}
```

---

## 여러 Repository를 사용하는 UseCase

```ts
// 대출 신청 시 사용자 정보도 함께 필요한 경우
@injectable()
export class ApplyLoanWithUserValidationUseCase {
  constructor(
    @inject('ILoanRepository')
    private readonly loanRepository: ILoanRepository,

    @inject('IAuthRepository')
    private readonly authRepository: IAuthRepository
  ) {}

  async execute(input: ApplyLoanInput): Promise<Loan> {
    // 사용자 신용 점수 확인
    const user = await this.authRepository.getMe();
    if (user.creditScore < MINIMUM_CREDIT_SCORE) {
      throw new DomainError('신용 점수가 부족합니다.');
    }

    return this.loanRepository.applyLoan(input);
  }
}
```

---

## UseCase 에러 처리 원칙

### 도메인 에러는 DomainError로 던지기
```ts
// core/error/AppError.ts 의 DomainError 사용
import { DomainError } from '@core/error/AppError';

async execute(amount: number): Promise<void> {
  if (amount <= 0) {
    throw new DomainError('금액은 0보다 커야 합니다.');
  }
}
```

### UseCase에서 try/catch 금지 (호출 측에 위임)
```ts
// ❌ UseCase 내부에서 catch 후 덮어쓰기 금지
async execute(): Promise<User> {
  try {
    return await this.authRepository.login(credentials);
  } catch (e) {
    return null;    // 에러를 삼켜버림 - 금지
  }
}

// ✅ 그냥 throw (presentation 훅에서 처리)
async execute(): Promise<User> {
  return this.authRepository.login(credentials);  // 에러는 위로 전파
}
```

---

## Presentation에서 UseCase 호출 방법

```ts
// features/loan/presentation/hooks/useLoan.ts
import { container } from '@core/di/container';
import { ApplyLoanUseCase } from '../domain/usecases/ApplyLoanUseCase';

export const useLoan = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const applyLoan = async (input: ApplyLoanInput) => {
    setIsLoading(true);
    setError(null);
    try {
      const useCase = container.resolve(ApplyLoanUseCase);
      const result = await useCase.execute(input);
      return result;
    } catch (e) {
      if (e instanceof DomainError) {
        setError(e.message);          // 도메인 에러: UI에 표시
      } else {
        setError('서버 오류가 발생했습니다.');  // 기술적 에러: 일반 메시지
      }
    } finally {
      setIsLoading(false);
    }
  };

  return { applyLoan, isLoading, error };
};
```

---

## UseCase 파일 위치 요약

```
features/{feature}/domain/usecases/
├── LoginUseCase.ts
├── LogoutUseCase.ts
├── ApplyLoanUseCase.ts
├── GetLoanStatusUseCase.ts
└── MoveCharacterUseCase.ts
```
