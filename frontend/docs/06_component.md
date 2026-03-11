# 🧩 컴포넌트 설계 원칙

## 개요

컴포넌트는 **UI 렌더링에만 집중**한다.  
비즈니스 로직은 UseCase로, 상태 조합은 커스텀 훅으로, 전역 공유는 Store로 분리한다.

---

## 컴포넌트 분류

| 종류 | 위치 | 역할 |
|------|------|------|
| **Page** | `features/{f}/presentation/pages/` | 라우트 진입점, 레이아웃 조합 |
| **Feature Component** | `features/{f}/presentation/components/` | 특정 Feature 전용 UI |
| **Shared Component** | `shared/components/` | 여러 Feature에서 재사용되는 UI |

---

## 컴포넌트 작성 원칙

### 1. 단일 책임
하나의 컴포넌트는 하나의 UI 역할만 담당한다.

```tsx
// ✅ 단일 책임 - 폼 렌더링만
export const LoginForm: React.FC<LoginFormProps> = ({ onSubmit, isLoading }) => {
  return (
    <form onSubmit={onSubmit}>
      <input name="email" type="email" />
      <input name="password" type="password" />
      <Button type="submit" isLoading={isLoading}>로그인</Button>
    </form>
  );
};

// ❌ 다중 책임 - API 호출 + 유효성 검사 + 렌더링 혼재
export const LoginForm: React.FC = () => {
  const handleSubmit = async () => {
    const res = await axios.post('/auth/login', formData);  // ← 여기 있으면 안 됨
    localStorage.setItem('token', res.data.token);           // ← 여기도 안 됨
  };
};
```

### 2. Props를 통한 제어 - 상위에서 주입
```tsx
// ✅ 올바른 예: 상태와 핸들러를 Props로 받음
interface LoanFormProps {
  onSubmit: (data: LoanFormData) => void;
  isLoading: boolean;
  error: string | null;
}

export const LoanForm: React.FC<LoanFormProps> = ({ onSubmit, isLoading, error }) => {
  const [amount, setAmount] = useState('');

  return (
    <div>
      {error && <ErrorMessage message={error} />}
      <input value={amount} onChange={(e) => setAmount(e.target.value)} />
      <Button onClick={() => onSubmit({ amount: Number(amount) })} isLoading={isLoading}>
        신청하기
      </Button>
    </div>
  );
};
```

### 3. 컴포넌트는 store를 직접 구독하지 않는다 (Page/훅 위임)
```tsx
// ❌ 컴포넌트에서 Store 직접 접근
export const LoanForm: React.FC = () => {
  const user = useAuthStore((s) => s.user);    // ← 컴포넌트에서 직접 금지
  const { applyLoan } = useLoanStore();
};

// ✅ 훅에서 처리 후 Props로 내려줌
// LoanPage.tsx (or useLoan 훅)
export const LoanPage: React.FC = () => {
  const { applyLoan, isLoading, error } = useLoan();
  return <LoanForm onSubmit={applyLoan} isLoading={isLoading} error={error} />;
};
```

---

## Page 컴포넌트 구조

Page는 레이아웃 조합과 훅 연결만 담당한다.

```tsx
// features/loan/presentation/pages/LoanPage.tsx
import React from 'react';
import { useLoan } from '../hooks/useLoan';
import { LoanForm } from '../components/LoanForm';
import { LoanStatusCard } from '../components/LoanStatusCard';

export const LoanPage: React.FC = () => {
  const { loanList, applyLoan, isLoading, error } = useLoan();

  return (
    <main>
      <h1>대출 신청</h1>
      <LoanForm onSubmit={applyLoan} isLoading={isLoading} error={error} />
      <section>
        {loanList.map((loan) => (
          <LoanStatusCard key={loan.id} loan={loan} />
        ))}
      </section>
    </main>
  );
};
```

---

## Shared 컴포넌트 설계

공통 컴포넌트는 **완전히 제어 가능(controlled)** 하게 설계한다.

```tsx
// shared/components/Button/Button.tsx
interface ButtonProps {
  children: React.ReactNode;
  onClick?: () => void;
  type?: 'button' | 'submit' | 'reset';
  variant?: 'primary' | 'secondary' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  isLoading?: boolean;
  disabled?: boolean;
  className?: string;
}

export const Button: React.FC<ButtonProps> = ({
  children,
  variant = 'primary',
  size = 'md',
  isLoading = false,
  disabled = false,
  className,
  ...rest
}) => {
  return (
    <button
      className={clsx(styles.button, styles[variant], styles[size], className)}
      disabled={disabled || isLoading}
      {...rest}
    >
      {isLoading ? <Spinner /> : children}
    </button>
  );
};
```

---

## 컴포넌트 Props 설계 규칙

```tsx
// ✅ 명확한 Props 타입 정의 (interface 사용)
interface CharacterProps {
  character: Character;            // Entity 타입 직접 사용 가능
  onMove: (direction: Direction) => void;
  isCurrentPlayer: boolean;
}

// ✅ children은 React.ReactNode
interface CardProps {
  title: string;
  children: React.ReactNode;
}

// ✅ 이벤트 핸들러는 on 접두사
interface FormProps {
  onSubmit: (data: FormData) => void;
  onChange?: (field: string, value: string) => void;
}

// ❌ any 타입 금지
interface BadProps {
  data: any;
  handler: any;
}
```

---

## CSS Module 사용 규칙

```
컴포넌트폴더/
├── Character.tsx
├── Character.module.css
└── index.ts
```

```tsx
// Character.tsx
import styles from './Character.module.css';

export const Character: React.FC<CharacterProps> = ({ character }) => {
  return (
    <div
      className={clsx(
        styles.character,
        styles[character.state],          // 상태별 스타일
        { [styles.currentPlayer]: isCurrentPlayer }
      )}
    >
      <img src={character.spriteUrl} alt={character.name} />
    </div>
  );
};
```

```css
/* Character.module.css */
.character { ... }
.character.idle { ... }
.character.walking { ... }
.currentPlayer { ... }
```

---

## 컴포넌트 성능 최적화 원칙

```tsx
// 1. 무거운 컴포넌트는 React.memo로 감싸기
export const LoanCard = React.memo<LoanCardProps>(({ loan }) => {
  return <div>...</div>;
});

// 2. 콜백은 useCallback (자식에게 props로 내려줄 때)
const handleSubmit = useCallback((data: LoanFormData) => {
  applyLoan(data);
}, [applyLoan]);

// 3. 무거운 연산은 useMemo
const sortedLoans = useMemo(
  () => loans.sort((a, b) => b.createdAt - a.createdAt),
  [loans]
);

// ❌ 과최적화 금지 - 단순 렌더링엔 memo 불필요
export const SimpleLabel = React.memo(({ text }: { text: string }) => (
  <span>{text}</span>
));
```

---

## 컴포넌트 exports 규칙

```ts
// ✅ named export 사용 (default export 지양)
export const LoginForm: React.FC<LoginFormProps> = () => { ... };

// index.ts를 통한 re-export
// features/auth/presentation/components/index.ts
export { LoginForm } from './LoginForm';
export { AuthHeader } from './AuthHeader';
```

---

## 금지 패턴 요약

```tsx
// ❌ 컴포넌트 내부에서 API 직접 호출
const MyComponent = () => {
  useEffect(() => {
    axios.get('/api/data').then(...);    // UseCase → 훅 → 컴포넌트 순으로
  }, []);
};

// ❌ default export (tree-shaking, 네이밍 혼란 방지)
export default LoginForm;

// ❌ 인라인 스타일 남용
<div style={{ color: 'red', fontSize: '14px', marginTop: '10px' }}>

// ❌ Props drilling 3단계 초과 → Store 또는 Context 활용
<A prop={x}>
  <B prop={x}>
    <C prop={x}>        // 여기까지 오면 구조 재고
```
