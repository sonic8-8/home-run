# 📐 프론트엔드 설계 원칙 인덱스

> **스택**: React + TypeScript + Zustand + tsyringe  
> **아키텍처**: Feature 기반 Clean Architecture  
> **작성일**: 2026-03-10

---

## 문서 목록

| 번호 | 문서 | 핵심 내용 |
|------|------|----------|
| 01 | [폴더 구조](./01_folder-structure.md) | feature/shared/core/app 분리, 계층 의존 방향, barrel export |
| 02 | [네이밍 컨벤션](./02_naming-convention.md) | 계층별 접두사/접미사, 파일명 케이스, 금지 패턴 |
| 03 | [UseCase](./03_usecase.md) | 단일 책임 execute(), DI 주입, 에러 전파 규칙 |
| 04 | [DI (tsyringe)](./04_di-tsyringe.md) | 컨테이너 설정, 토큰 관리, 테스트 mock 교체 |
| 05 | [상태 관리 (Zustand)](./05_state-management-zustand.md) | Store 위치 결정, 선택적 구독, UseCase 연계 |
| 06 | [컴포넌트](./06_component.md) | 단일 책임, Props 설계, Page vs Component, CSS Module |
| 07 | [TypeScript](./07_typescript.md) | any 금지, 유틸리티 타입, Discriminated Union, 브랜드 타입 |
| 08 | [Entity](./08_entities.md) | 불변성, 값 객체, 팩토리 함수, 브랜드 타입, Entity vs Model |
| 09 | [Repository](./09_repositories.md) | I 접두사 인터페이스, Impl 구현체, Model→Entity 변환 책임 |
| 10 | [Model (DTO)](./10_models.md) | API 응답/요청 형태, snake_case 허용, 변환 위치 |
| 11 | [커스텀 훅](./11_hooks.md) | shared훅 vs feature훅 차이, UseCase 호출 패턴, 에러 처리 전략 |
| 12 | [Router](./12_router.md) | 경로 상수 중앙관리, Guard 컴포넌트, Lazy Loading |
| 13 | [core/network](./13_core-network.md) | apiClient 설정, interceptors 공통 처리 (토큰/에러 자동화) |
| 14 | [core/store](./14_core-store.md) | Store 위치 결정 기준, authStore/gameStore/uiStore 작성법 |
| 15 | [core/error](./15_core-error.md) | 에러 분류 체계, 계층별 발생/처리 위치, 타입 가드 |
| 16 | [shared/utils](./16_shared-utils.md) | formatter/validator/calculator/storage, 순수함수 원칙 |

---

## 핵심 의존 방향 요약

```
[Presentation] ──→ [Domain] ←── [Data]
   hooks              entities      datasources
   pages              usecases      repositories
   components         I{Repository} models
        ↓
   [Zustand Store]           ↑
   (전역 상태 업데이트)    [DI Container]
                          (tsyringe)
```

---

## 계층 체크리스트

개발 시작 전 아래 질문에 답하여 코드 위치를 결정한다.

```
❓ "이것은 어디에 속하는가?"

→ 비즈니스 규칙/제약 조건인가?
  ✅ domain/usecases/ 또는 domain/entities/

→ API 호출 또는 로컬 저장인가?
  ✅ data/datasources/

→ 서버 응답/요청 형태인가?
  ✅ data/models/

→ UI를 어떻게 보여줄지인가?
  ✅ presentation/components/ 또는 presentation/pages/

→ UI와 비즈니스 로직의 연결인가?
  ✅ presentation/hooks/

→ 여러 feature에 걸친 전역 공유인가?
  ✅ core/store/ (Zustand) 또는 shared/

→ 앱 기반 인프라인가? (HTTP, DI, 에러)
  ✅ core/
```

---

## 빠른 참조

### 새 Feature 추가 순서
```
1. domain/entities/    → Entity 정의
2. domain/repositories/ → IRepository 인터페이스
3. domain/usecases/    → UseCase 작성
4. data/models/        → DTO 모델
5. data/datasources/   → API/Local 구현
6. data/repositories/  → IRepository 구현체
7. core/di/container.ts → DI 등록
8. presentation/hooks/ → UseCase 연결 훅
9. presentation/components/ → UI 컴포넌트
10. presentation/pages/ → 페이지 조합
11. app/Router.tsx     → 라우트 연결
```

### 네이밍 빠른 참조
```
Entity:          User, Loan, Character          (접두/미사 없음)
Interface:       IAuthRepository                (I 접두사)
UseCase:         LoginUseCase                   (UseCase 접미사)
Model(DTO):      UserModel                      (Model 접미사)
DataSource:      AuthRemoteDataSource           (DataSource 접미사)
Impl:            AuthRepositoryImpl             (Impl 접미사)
Page:            LoginPage                      (Page 접미사)
Hook:            useAuth, useLoan               (use 접두사)
Store:           authStore, gameStore           (Store 접미사)
Route 경로:      ROUTES.LOAN, ROUTES.LOAN_DETAIL(id)  (상수 중앙 관리)
```
