# 🎨 CSS 스타일링 설계 원칙

## 이 프로젝트의 스타일링 방식

**CSS Module**을 기본으로 사용한다.

```
CSS Module을 쓰는 이유
  ├── 클래스명이 자동으로 고유해진다 (다른 컴포넌트와 이름 충돌 없음)
  ├── 어떤 CSS가 어떤 컴포넌트에 속하는지 명확하다
  ├── TypeScript와 함께 클래스명 자동완성 지원
  └── 빌드 시 사용하지 않는 CSS 자동 제거
```

---

## CSS Module 기본 사용법

### 파일 생성 규칙

컴포넌트와 **같은 폴더**에 **같은 이름**으로 `.module.css` 파일을 만든다.

```
components/
└── LoginForm/
    ├── LoginForm.tsx          ← 컴포넌트
    ├── LoginForm.module.css   ← 스타일 (항상 같은 이름)
    └── index.ts
```

### 기본 작성법

```css
/* LoginForm.module.css */

.container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px;
  background-color: #ffffff;
  border-radius: 8px;
}

.title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
}

.inputGroup {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input {
  width: 100%;
  height: 48px;
  padding: 0 16px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 16px;
  outline: none;
  transition: border-color 0.2s;
}

.input:focus {
  border-color: #4f46e5;   /* 포커스 시 테두리 색 변경 */
}

.input.error {
  border-color: #ef4444;   /* 에러 상태 */
}

.errorMessage {
  font-size: 12px;
  color: #ef4444;
}

.submitButton {
  width: 100%;
  height: 48px;
  background-color: #4f46e5;
  color: #ffffff;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.submitButton:hover {
  background-color: #4338ca;
}

.submitButton:disabled {
  background-color: #a5b4fc;
  cursor: not-allowed;
}
```

```tsx
// LoginForm.tsx
import styles from './LoginForm.module.css';

export const LoginForm: React.FC<LoginFormProps> = ({ onSubmit, isLoading, error }) => {
  return (
    <div className={styles.container}>
      <h1 className={styles.title}>로그인</h1>

      <div className={styles.inputGroup}>
        <input
          className={styles.input}
          type="email"
          placeholder="이메일"
        />
      </div>

      <button
        className={styles.submitButton}
        disabled={isLoading}
      >
        로그인
      </button>
    </div>
  );
};
```

---

## 조건부 스타일 적용

상태에 따라 클래스를 다르게 적용할 때는 **clsx** 라이브러리를 사용한다.

```bash
npm install clsx
```

### clsx 사용 패턴

```tsx
import clsx from 'clsx';
import styles from './Input.module.css';

interface InputProps {
  hasError?: boolean;
  disabled?: boolean;
  size?: 'sm' | 'md' | 'lg';
}

export const Input: React.FC<InputProps> = ({ hasError, disabled, size = 'md' }) => {
  return (
    <input
      className={clsx(
        styles.input,              // 항상 적용
        styles[size],              // 동적 클래스 (sm, md, lg)
        hasError && styles.error,  // 조건부 클래스
        disabled && styles.disabled
      )}
    />
  );
};
```

```css
/* Input.module.css */
.input { ... }          /* 기본 스타일 */
.sm { height: 32px; }
.md { height: 40px; }
.lg { height: 48px; }
.error { border-color: #ef4444; }
.disabled { opacity: 0.5; }
```

### 조건부 스타일 패턴 종류

```tsx
// 1. 단순 조건 (boolean)
className={clsx(styles.button, isActive && styles.active)}

// 2. 객체 형태 (가독성 좋음)
className={clsx(styles.button, {
  [styles.active]: isActive,
  [styles.disabled]: disabled,
  [styles.loading]: isLoading,
})}

// 3. 동적 키 (enum/union 타입 기반)
// variant: 'primary' | 'secondary' | 'danger'
className={clsx(styles.button, styles[variant])}

// 4. 여러 방법 혼합
className={clsx(
  styles.card,
  styles[size],
  isSelected && styles.selected,
  { [styles.highlighted]: isHighlighted }
)}
```

---

## CSS 변수 (Custom Properties) — 디자인 토큰

색상, 폰트, 간격 등 **반복되는 값**은 CSS 변수로 중앙 관리한다.

```css
/* src/assets/styles/variables.css */
:root {
  /* ── 색상 ── */
  --color-primary: #4f46e5;
  --color-primary-hover: #4338ca;
  --color-primary-light: #e0e7ff;

  --color-danger: #ef4444;
  --color-danger-light: #fee2e2;

  --color-success: #22c55e;
  --color-warning: #f59e0b;

  --color-text-primary: #1a1a1a;
  --color-text-secondary: #6b7280;
  --color-text-disabled: #9ca3af;

  --color-border: #e5e7eb;
  --color-border-focus: #4f46e5;

  --color-bg-primary: #ffffff;
  --color-bg-secondary: #f9fafb;
  --color-bg-disabled: #f3f4f6;

  /* ── 폰트 크기 ── */
  --font-size-xs: 12px;
  --font-size-sm: 14px;
  --font-size-md: 16px;
  --font-size-lg: 18px;
  --font-size-xl: 20px;
  --font-size-2xl: 24px;

  /* ── 간격 ── */
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 16px;
  --spacing-lg: 24px;
  --spacing-xl: 32px;
  --spacing-2xl: 48px;

  /* ── 테두리 반경 ── */
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --radius-full: 9999px;

  /* ── 그림자 ── */
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.1);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.12);

  /* ── 전환 ── */
  --transition-fast: 0.15s ease;
  --transition-normal: 0.2s ease;
}
```

```css
/* 컴포넌트에서 CSS 변수 사용 */
.button {
  background-color: var(--color-primary);       /* 직접 색상 코드 대신 변수 */
  border-radius: var(--radius-md);
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: var(--font-size-md);
  transition: background-color var(--transition-normal);
}

.button:hover {
  background-color: var(--color-primary-hover);
}
```

### variables.css 적용 방법

```tsx
// src/main.tsx
import '@/assets/styles/variables.css';   // 앱 진입점에서 한 번만 import
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './app/App';
```

---

## 전역 스타일

CSS Reset, 기본 body 스타일 등 전역 설정은 별도 파일로 분리한다.

```css
/* src/assets/styles/global.css */

/* CSS Reset */
*, *::before, *::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html {
  font-size: 16px;
  -webkit-text-size-adjust: 100%;
}

body {
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, sans-serif;
  font-size: var(--font-size-md);
  color: var(--color-text-primary);
  background-color: var(--color-bg-secondary);
  line-height: 1.5;
}

/* 기본 링크 스타일 제거 */
a {
  color: inherit;
  text-decoration: none;
}

/* 기본 버튼 스타일 제거 */
button {
  background: none;
  border: none;
  cursor: pointer;
  font: inherit;
}

/* 이미지 */
img, video {
  max-width: 100%;
  display: block;
}
```

```tsx
// src/main.tsx
import '@/assets/styles/variables.css';
import '@/assets/styles/global.css';    // variables 다음에 import
```

---

## 파일 구조

```
src/assets/styles/
├── variables.css   # 디자인 토큰 (색상, 간격, 폰트 등)
└── global.css      # 전역 스타일 (Reset, body 기본 설정)

features/auth/presentation/components/
├── LoginForm/
│   ├── LoginForm.tsx
│   └── LoginForm.module.css    ← feature 컴포넌트 스타일
└── AuthHeader/
    ├── AuthHeader.tsx
    └── AuthHeader.module.css

shared/components/
├── Button/
│   ├── Button.tsx
│   └── Button.module.css       ← 공통 컴포넌트 스타일
└── Modal/
    ├── Modal.tsx
    └── Modal.module.css
```

---

## 게임 씬 컴포넌트 스타일링

게임 캐릭터처럼 **상태가 많은 컴포넌트**는 상태를 CSS 클래스로 표현한다.

```css
/* Character.module.css */
.character {
  position: absolute;
  width: 48px;
  height: 48px;
  image-rendering: pixelated;   /* 픽셀 아트 선명하게 */
}

/* 캐릭터 상태별 스타일 */
.character.idle {
  animation: breathe 2s ease-in-out infinite;
}

.character.walking {
  animation: walk 0.4s steps(4) infinite;
}

.character.running {
  animation: walk 0.2s steps(4) infinite;
}

.character.stunned {
  opacity: 0.5;
  filter: grayscale(100%);
}

/* 현재 플레이어 강조 */
.currentPlayer {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

/* 이동 방향 (스프라이트 반전) */
.facingLeft {
  transform: scaleX(-1);
}

@keyframes breathe {
  0%, 100% { transform: translateY(0); }
  50%       { transform: translateY(-2px); }
}

@keyframes walk {
  from { background-position-x: 0; }
  to   { background-position-x: -192px; }   /* 스프라이트 시트 4프레임 */
}
```

```tsx
// Character.tsx
import clsx from 'clsx';
import styles from './Character.module.css';

export const Character: React.FC<CharacterProps> = ({
  character,
  isCurrentPlayer,
}) => {
  return (
    <div
      className={clsx(
        styles.character,
        styles[character.state],                         // idle, walking, running...
        isCurrentPlayer && styles.currentPlayer,
        character.facingDirection === 'left' && styles.facingLeft
      )}
      style={{
        left: character.position.x,
        top: character.position.y,
      }}
    />
  );
};
```

---

## 반응형 스타일

```css
/* 모바일 우선 (Mobile First) 방식 권장 */
.container {
  padding: var(--spacing-md);      /* 기본: 모바일 */
}

@media (min-width: 768px) {
  .container {
    padding: var(--spacing-lg);    /* 태블릿 이상 */
  }
}

@media (min-width: 1024px) {
  .container {
    padding: var(--spacing-xl);    /* 데스크탑 이상 */
    max-width: 1200px;
    margin: 0 auto;
  }
}
```

### 브레이크포인트 변수로 관리

```css
/* variables.css에 추가 */
:root {
  --breakpoint-sm: 640px;
  --breakpoint-md: 768px;
  --breakpoint-lg: 1024px;
  --breakpoint-xl: 1280px;
}
```

---

## 클래스명 네이밍 규칙

CSS Module 내 클래스명은 **camelCase**를 사용한다.

```css
/* ✅ camelCase */
.submitButton { }
.errorMessage { }
.inputGroup { }
.isActive { }

/* ❌ kebab-case (CSS Module에서는 camelCase 권장) */
.submit-button { }
.error-message { }
```

이유: TypeScript에서 `styles.submitButton`으로 접근할 수 있지만  
`styles['submit-button']`처럼 문자열 인덱스를 써야 하면 자동완성이 안 된다.

---

## 금지 패턴

```tsx
// ❌ 인라인 스타일 남용
<div style={{ color: 'red', fontSize: '14px', marginTop: '10px' }}>

// ✅ CSS Module 클래스 사용
<div className={styles.errorText}>

// ───────────────────────────────────────────────

// ❌ 전역 클래스명 사용 (다른 컴포넌트와 충돌 위험)
<div className="container">
<div className="button primary">

// ✅ CSS Module 클래스 사용
<div className={styles.container}>
<button className={clsx(styles.button, styles.primary)}>

// ───────────────────────────────────────────────

// ❌ CSS 변수 대신 색상 하드코딩
.button {
  background-color: #4f46e5;   /* 나중에 색상 변경 시 전부 찾아야 함 */
}

// ✅ CSS 변수 사용
.button {
  background-color: var(--color-primary);
}

// ───────────────────────────────────────────────

// ❌ !important 사용
.title {
  color: red !important;   /* 디버깅 어려워짐 */
}
```
