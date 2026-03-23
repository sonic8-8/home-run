## 범위
- 이 규칙은 `backend`의 Java/Spring 코드에 적용한다.
- `frontend`는 기존 프론트엔드 구조와 패턴을 우선 따른다.

## 라벨링
- 기본 (라벨 없음): `핵심 규칙`을 적용하고, 필요하면 `권장 사항`을 참고한다.
- `[full]`: `핵심 규칙`, `권장 사항`, `도메인 용어 사전`을 최대한 반영해 상세하게 답변한다.

## 핵심 규칙
- 패키지는 계층을 먼저 나누고, 계층 안에서 도메인별로 나눈다.
- 최상위 패키지는 `api`, `domain`, `client`, `config`를 사용하고, 애플리케이션 시작 클래스는 루트 패키지에 둔다.
- Controller는 `api/controller/{domain}`, Controller Request DTO는 `api/controller/{domain}/request`에 둔다.
- Service는 `api/service/{domain}`, Service Request/Response DTO는 `api/service/{domain}/request`, `api/service/{domain}/response`에 둔다.
- `domain` 하위는 애그리거트 루트 기준으로 먼저 묶는다. Entity / Enum / Repository는 기본적으로 같은 애그리거트 패키지에 두고, `entity`, `repository`, `enum` 같은 기술 분류용 하위 패키지는 만들지 않는다.
- 외부 시스템 연동은 `client`에 두고, 스프링 설정만 `config`에 둔다.
- 애그리거트 내부 클래스가 많을 때만 해당 애그리거트 디렉토리 안에서 `turn`, `report`, `accountauth`처럼 의미별 하위 패키지로 나눈다.
- 독립 생명주기, 독립 트랜잭션 경계, 독립 조회 요구가 생기면 별도 도메인 패키지로 승격한다. 그렇지 않으면 상위 애그리거트 내부에 둔다.
- 이력성 데이터나 보조 하위 도메인은 `domain/history/...`처럼 의미 단위로 묶되, 상위 애그리거트에 종속되면 그 애그리거트 내부 하위 패키지를 우선 검토한다.
- Controller는 HTTP 요청 수신, 입력 검증, Service 호출, `ApiResponse` 반환만 담당한다.
- Service는 유스케이스 실행, 트랜잭션 처리, Repository 조합, Domain과 DTO 연결을 담당한다.
- Service 명명은 기본적으로 `...Service`를 사용하고, 조회/변경 책임 분리가 필요해질 때 `...QueryService`, `...CommandService`로 구체화한다.
- Domain은 핵심 상태와 비즈니스 규칙을 가진다. Repository는 조회/저장 책임에 집중한다.
- Controller Request DTO와 Service DTO는 분리한다.
- 보호 API를 추가하거나 보안 설정을 변경할 때는, 컨트롤러와 서비스가 요청 파라미터의 `userId`/`userKey` 대신 인증 principal을 사용해야 하는지 함께 검토한다.
- API는 엔티티를 직접 반환하지 않고 Response DTO로 변환한 뒤 공통 `ApiResponse`로 감싼다.
- 예외 응답은 공통 `ErrorResponse`로 반환한다.
- Validation 예외는 `ErrorResponse`의 `errors` 목록에 필드별 상세를 포함하는 것을 우선 검토한다.
- 새 코드는 기존 구조와 네이밍을 우선 따르고, 과한 추상화보다 명확한 구현을 우선한다.
- `Reader`, `Provider` 같은 추가 추상화는 기본 규칙으로 도입하지 않고, 구현 교체 필요나 외부 시스템 경계가 분명할 때만 예외적으로 검토한다.
- 이름은 `Controller`, `Service`, `Client`, `Repository`, `Request`, `ServiceRequest`, `Response`, `Config`, `Test`, `TestSupport` 접미사를 사용한다.
- Value Object는 기본값으로 만들지 않고, 도메인 의미, 불변성/생성 검증, 값 비교 규칙이 분명할 때만 도입한다.

### 패키지 설계 원칙
- 이름: `계층 분리 + 애그리거트 중심 도메인 패키징`
- 최상위는 `api`, `domain`, `client`, `config`처럼 계층으로 나눈다.
- `domain` 하위는 DB 테이블 개수보다 애그리거트 경계를 우선한다.
- 하위 개념은 `domain` 바로 아래에 평평하게 두지 말고, 가능하면 상위 애그리거트 내부 하위 패키지에 둔다.

좋은 예시
- `domain/user/User`, `domain/user/UserRepository`, `domain/user/auth/RefreshToken`
- `domain/gamesession/GameSession`, `domain/gamesession/turn/GameTurnSlot`, `domain/gamesession/report/GameReport`

지양 예시
- `domain/turn`, `domain/settlement`, `domain/report`를 독립 애그리거트 근거 없이 `domain` 바로 아래에 평평하게 두는 구조

## 권장 사항

### 코드 컨벤션 (BE)
- indent depth는 가급적 2 이하로 유지한다.
- `else`, `switch/case`, 삼항 연산자 사용은 지양하고 Early return 패턴을 우선 검토한다.
- 가급적 1메서드 1기능 원칙을 지향한다.
- 예외는 특별한 이유가 없으면 static factory보다 `throw new ExceptionType(...)` 형태를 우선 사용한다.
- 핵심 도메인의 원시값과 문자열은 VO 후보로 먼저 검토한다.
- 비즈니스 로직이 포함된 컬렉션은 일급 컬렉션으로 포장할지 검토한다.
- 객체 생성 시 정적 팩토리 메서드 패턴을 우선 검토한다.
- Controller는 `@RestController`, `@RequiredArgsConstructor`, Service는 `@Service`, `@RequiredArgsConstructor`를 기본으로 검토한다.
- 엔티티는 `@Getter`, `@Entity`, `@NoArgsConstructor(access = PROTECTED)` 패턴을 우선 검토하고, 생성은 Builder 또는 정적 팩토리 메서드를 우선 검토한다.
- Validation 메시지는 DTO에 하드코딩하지 않고 메시지 키를 사용하며, 실제 문구는 `ValidationMessages.properties`에서 관리하는 것을 우선 검토한다.

### 금융 도메인 규칙
- 돈 계산은 정밀도 문제 방지를 위해 `BigDecimal` 사용을 우선 검토한다. `double`/`float`는 지양한다.
- 금액 데이터는 `Money` VO로 포장할지 우선 검토한다.

### 빌드/테스트
- 테스트 실행은 `./gradlew test`를 우선 사용한다.
- 프론트엔드 패키지 매니저는 `npm` 사용을 우선한다.
- 테스트는 JUnit 5, AssertJ를 사용하고, `@DisplayName` 한글 문장, `given / when / then`, Controller 슬라이스 테스트, Service 통합 테스트, `*TestSupport` 공통 설정, 저장소 테스트 후 상태 정리 규칙을 우선 따른다.
- 테스트는 계층 책임에 맞춰 분리한다.
- Controller 테스트는 `@WebMvcTest` 기반 슬라이스 테스트를 우선 검토한다.
- Service 테스트는 `@SpringBootTest` 기반 통합 테스트를 우선 검토한다.
- 외부 HTTP Client 테스트는 `@RestClientTest`와 `MockRestServiceServer` 기반 슬라이스 테스트를 우선 검토한다.
- `@ConfigurationProperties` 테스트는 properties bean을 직접 생성하기보다 `properties`, `@TestPropertySource`, `application-test.yml`, `@DynamicPropertySource`로 property source를 주입하는 방식을 우선 검토한다.
- Config 테스트는 기본적으로 `ApplicationContextRunner`로 bean 생성/조건부 등록/properties binding을 검증하는 방식을 우선 검토한다.
- Security, MVC, Filter Chain처럼 실제 애플리케이션 동작 결과까지 검증해야 하는 설정은 `@SpringBootTest` 기반 통합 테스트를 우선 검토한다.
- Parser, Mapper, Policy, Calculator처럼 순수 로직 중심 클래스는 Spring 컨텍스트 없이 unit test를 우선 검토한다.
- `*TestSupport`는 중복되는 테스트 패턴이 반복될 때 추출을 검토한다.

### 커밋 메시지
- 백엔드 형식: `[BE] type(scope): 설명 (Jira 티켓번호)`
- 프론트엔드 형식: `[FE] type(scope): 설명 (Jira 티켓번호)`
- type 목록: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`

## 도메인 용어 사전

> 코드에서 사용하는 클래스명, 메서드명, DB 컬럼명은 아래 영문 명칭을 우선 참고한다.

### 1. 게임 구조
| 한글 | 영문 (코드용) | 설명 |
| :--- | :--- | :--- |
| 턴 | `Turn` | 게임 내 1개월. 슬롯 3개 포함 |
| 슬롯 | `Slot` | 상순/중순/하순. 행동 1개 배치 |
| 행동 | `Action` | 슬롯에 배치하는 활동 |
| 정산 | `Settlement` | 턴 종료 시 처리 |
| 게임 세션 | `GameSession` | 1회 플레이 전체 상태 |

### 2. 캐릭터
| 한글 | 영문 (코드용) | 설명 |
| :--- | :--- | :--- |
| 체력 | `Health` | `0~100` |
| 피로도 | `Fatigue` | `0~100` |
| 스트레스 | `Stress` | `0~100` |
| 지식 | `Knowledge` | `0~100` |
| 행복도 | `Happiness` | `0~100` |
| 번아웃 | `Burnout` | 피로 `80+` AND 스트레스 `80+` 상태 |

### 3. 커리어
| 한글 | 영문 (코드용) | 설명 |
| :--- | :--- | :--- |
| 직장 유형 | `JobType` | `SMALL_BIZ`, `MID_BIZ`, `LARGE_BIZ`, `STARTUP`, `FREELANCER` |
| 직함 | `JobTitle` | 근속 기반 자동 부여 |
| 연봉 협상 | `SalaryNegotiation` | 연 1회, 슬롯 소모 |
| 협상 준비도 | `NegotiationPreparation` | 공부/네트워킹 횟수 기반 |
| 이직 | `JobTransfer` | 직장 유형 변경 + 근속 리셋 |
| 강제 퇴사 | `ForcedResignation` | 체력 `0~9` 트리거 |
| 실업 급여 | `UnemploymentBenefit` | 퇴사 전 월급 50%, 3턴 |

### 4. 금융
| 한글 | 영문 (코드용) | 설명 |
| :--- | :--- | :--- |
| 현금 | `Cash` | 수시입출금 잔액 |
| 순자산 | `NetAssets` | 현금 + 주식 + 부동산 - 대출 |
| 총자산 | `TotalAssets` | 부채를 포함한 전체 자산 |
| 주식 | `Stock` | 종목별 보유 수량 + 평균 매수가 |
| 환율 | `ExchangeRate` | 원/달러 기준 |
| 환전 스프레드 | `ExchangeSpread` | 매수 `+0.5%`, 매도 `-0.5%` |
| 싸피론 | `SsafyLoan` | 게임 전용 대출 |
| 연체 | `Overdue` | 현금 부족 → 강제 매도 후에도 부족 시 |

### 5. 주거
| 한글 | 영문 (코드용) | 설명 |
| :--- | :--- | :--- |
| 주거 상태 | `HousingType` | `NONE`, `STUDIO`, `VILLA`, `JEONSE_APT`, `OWNED_APT` |
| 보증금 | `Deposit` | 주거 진입 비용 |
| 관리비 | `MaintenanceFee` | 평균 관리비 |
| 서류 검토 | `DocumentReview` | 계약 시 서류 확인 UX |
| 함정 | `Trap` | 깡통전세, 과도한 근저당 등 |

### 6. 세계관
| 한글 | 영문 (코드용) | 설명 |
| :--- | :--- | :--- |
| 경제 사이클 | `EconomicCycle` | 세부 사이클 묶음 |
| 사이클 상태 | `CyclePhase` | `BOOM`, `CRISIS`, `RECOVERY` |
| 뉴스 | `News` | 턴 시작 시 노출 |
| 이벤트 | `GameEvent` | 랜덤 발생 |
| 전조 뉴스 | `ForeshadowNews` | 사이클 종료 전 힌트 |

### 7. 엔딩
| 한글 | 영문 (코드용) | 설명 |
| :--- | :--- | :--- |
| 클리어 | `CLEAR` | 목표 집 매수 |
| 파산 | `BANKRUPT` | 순자산 `≤ 0` or 연체 3턴 |
| 타임아웃 | `TIMEOUT` | 360턴 도달 |
| 압류 | `FORECLOSURE` | 연체로 인한 강제 자산 처분 |
