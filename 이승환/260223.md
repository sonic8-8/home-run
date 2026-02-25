# 2026년 02월 23일 월요일 학습 회고록

## 1. ⚾ 숫자 야구 미션 재설계

### 📌 배경

팀원들과 함께 **TDD(Test-Driven Development)** 및 **객체지향 프로그래밍(OOP)**을 연습하기 위해 지난주부터 함께 미션을 수행해왔습니다.

기존 미션은 "하나의 백지 프로젝트에서 TDD와 리팩토링을 동시에 하라"는 방식이었는데, 이 구조는 초심자에게 **인지 과부하**를 유발하고 앞선 스텝에서 설계를 잘못 잡으면 뒤의 스텝이 아예 막혀버리는 **병목 현상**이 발생했습니다.

이 문제를 해결하기 위해 『탤런트 코드』의 **심층 연습(Deep Practice)** 이론과 『함께 자라기』의 **작은 성공(Small Wins)** 원칙, 그리고 수학 강사 시절 경험을 바탕으로 미션을 2개의 파트로 나누었습니다.

- **Phase 1** (`part1-tdd-practice`): 최소한의 기능만 TDD 사이클로 구현하도록 유도하며 TDD 사이클 복습
- **Phase 2** (`part2-refactoring-gym`): 의도적으로 안티 패턴이 심어진 레거시 코드를, 테스트 그린 상태를 유지하며 리팩토링하도록 유도

## 2. Phase 1 미션 수행 내용 정리

### 🧩 최종 패키지 구조

```
baseball/
 ├── Application.java          // 프로그램 진입점 (객체 생성 담당)
 ├── Baseball.java             // 게임 흐름 제어 (Controller)
 ├── Umpire.java               // 스트라이크/볼 판정 (핵심 도메인)
 │
 ├── io/                       // 입출력 담당 (View 레이어)
 │    ├── InputHandler.java          // 입력 인터페이스
 │    ├── OutputHandler.java         // 출력 인터페이스
 │    ├── ConsoleInputHandler.java   // 콘솔 입력 구현체
 │    └── ConsoleOutputHandler.java  // 콘솔 출력 구현체
 │
 └── util/                     // 유틸리티 (검증, 변환, 생성)
      ├── InputValidator.java        // 입력값 검증
      ├── InputConverter.java        // 문자열 → 숫자 리스트 변환
      └── RandomNumbersGenerator.java // 랜덤 정답 생성
```

**설계 의도:** 입출력(`InputHandler`/`OutputHandler`)을 인터페이스로 추상화한 이유는 추후 콘솔(Console) 환경에서 웹(Web) 환경으로 확장할 가능성을 고려한 것입니다. 입출력 요구사항이 바뀌어도 비즈니스 로직을 수정할 필요가 없게끔 구현했습니다. (DIP, 의존성 역전 원칙)

---

### 🔥 미션을 수행하며 마주친 실수와 교훈들

#### 1. `==` vs `.equals()` — 자바의 가장 흔한 함정

**상황:** `Umpire`에서 이중 for문을 Stream으로 리팩토링하던 중, 볼 판정의 부정 조건을 `!=`로 작성했습니다.

```java
// ❌ Reference(메모리 주소) 비교 → 큰 숫자에서 예상치 못한 버그 발생
.filter(index -> target.get(index) != userInput.get(index))

// ✅ Value(값) 비교
.filter(index -> !target.get(index).equals(userInput.get(index)))
```

**교훈:** `List<Integer>`에서 꺼낸 원소는 **객체(Integer)**이므로, `==`/`!=`는 메모리 주소를 비교한다는 것을 알게 되었습니다. 반드시 `.equals()`를 사용해야 하며, 특히 `-128 ~ 127` 범위 밖의 숫자에서는 `==`가 예상과 다르게 `false`를 반환할 수 있다는 점을 배웠습니다.

---

#### 2. 배열 비교의 함정 — `result == new int[]{0, 3}`은 항상 `false`

**상황:** 게임 루프에서 3스트라이크 판정 시 게임을 종료하려 했으나, 아무리 맞춰도 게임이 끝나지 않았습니다.

```java
// ❌ 자바에서 배열은 객체 → == 는 주소 비교 → 항상 false
if (result == new int[]{0, 3}) { gameStatus = GameStatus.WINNING; }

// ✅ 요소를 직접 비교
if (result[1] == 3) { ... }

// ✅ 또는 Arrays.equals 사용
if (Arrays.equals(result, new int[]{0, 3})) { ... }
```

**교훈:** 자바에서 배열은 원시 타입이 아닌 **객체**이므로, `==`로 비교하면 항상 `false`가 된다는 것을 배웠습니다. 배열 내용을 비교할 때는 `Arrays.equals()`를 사용해야 합니다.

---

#### 3. `String.chars()`는 아스키코드를 반환한다

**상황:** `InputConverter`에서 사용자 입력 `"123"`을 `List<Integer>`로 변환했는데, 기대한 `[1, 2, 3]`이 아닌 `[49, 50, 51]`이 나왔습니다.

```java
// ❌ "123" → [49, 50, 51] (아스키코드)
userInput.chars().boxed().toList();

// ✅ "123" → [1, 2, 3] (실제 숫자)
userInput.chars()
    .map(Character::getNumericValue)  // 아스키코드 → 실제 숫자
    .boxed()                          // int → Integer (컬렉션에 담기 위해)
    .toList();
```

**교훈:** `String.chars()`가 문자의 유니코드(아스키) 값을 반환한다는 것을 알게 되었습니다. 실제 숫자로 변환하려면 `Character::getNumericValue`를 `.map()`에 적용해야 한다는 것을 배웠습니다.  
추가로, `boxed()`는 원시 타입(`int`) 스트림을 객체 타입(`Integer`) 스트림으로 변환하는 역할이며, `List<>`는 객체만 담을 수 있기 때문에 반드시 필요한 과정이라는 것도 함께 배웠습니다.

---

#### 4. 이중 for문을 Stream으로 개선하기 — `IntStream.range()` 활용

**상황:** `Umpire`의 스트라이크/볼 판정 로직이 이중 for문으로 작성되어 가독성이 떨어졌습니다.

**해결 과정:** IntStream.range()를 사용하여 인덱스를 스트림으로 변환하고, .filter()와 .count()를 사용하여 스트라이크와 볼의 수를 계산했습니다.

**교훈:** `Arrays.stream(배열)` 대신 **`IntStream.range(0, 3)`**을 사용하면 인덱스 자체를 스트림으로 돌릴 수 있다는 것을 알게 되었습니다. 외부 변수를 `++`로 증가시키는 대신, **`.filter()`로 조건을 걸고 `.count()`로 세는 것**이 함수형 프로그래밍의 방식이라는 것을 배웠습니다. 스트라이크용 스트림과 볼용 스트림을 **별도로 분리**하면 각각의 의도가 명확해진다는 점도 깨달았습니다

---

#### 5. 의존성 주입이 너무 많아질 때 — 객체 생성과 사용의 분리

**상황:** `Baseball` (게임 컨트롤러)의 생성자에 파라미터가 7개까지 늘어나 "생성자 과부하" 안티 패턴이 발생했습니다.

**해결 과정:**
1. `ConsoleInputHandler`가 내부에서 `InputValidator`와 `InputConverter`를 직접 생성하도록 변경 → 외부에서 주입할 파라미터 수를 7개 → 4개로 감소
2. `Application.java`(main)에서 객체를 **생성**하고, `Baseball.java`는 전달받은 객체를 **사용만** 하는 구조로 분리

**교훈:** "객체를 생성하는 책임"과 "객체를 사용하는 책임"을 분리하면, 각 클래스가 자기 역할에만 집중할 수 있다는 것을 배웠습니다. 의존성이 너무 많아진다고 느껴지면 **관련 있는 객체끼리 묶어서 한 쪽이 내부에서 생성하게 하는 것**도 좋은 방법이라는 것을 알게 되었습니다.

---

#### 6. 테스트를 작성하는 대상 vs 작성하지 않는 대상

| 대상 | 테스트 여부 | 이유 |
|---|---|---|
| `Umpire` | ⭕ | 핵심 비즈니스 로직. 판정 정확성이 게임의 전부 |
| `InputValidator` | ⭕ | 예외 상황이 많고, 잘못되면 게임 전체가 망가짐 |
| `InputConverter` | ⭕ | 아스키코드 변환 같은 미묘한 버그가 숨어있을 수 있음 |
| `RandomNumbersGenerator` | ⭕ | 3자리 + 중복 없음이라는 규칙을 보장해야 함 |
| `ConsoleInputHandler` | ❌ | `System.in` 의존적, 깨지기 쉬운 테스트가 됨 |
| `ConsoleOutputHandler` | ❌ | `System.out` 의존적, 출력 문구만 바뀌어도 테스트가 깨짐 |

**교훈:** 순수한 입출력(콘솔에 글씨 찍기, 키보드 읽기) 자체는 테스트하지 않되, 입출력 **전후의 처리 로직**(검증, 변환)은 반드시 테스트해야 한다는 기준을 세울 수 있었습니다.

---

## 📚 활용한 테스트 도구 요약

```java
// 값 검증
assertThat(result).containsExactly(0, 3);
assertThat(randoms).hasSize(3);
assertThat(randoms).doesNotHaveDuplicates();

// 예외 검증
assertThatThrownBy(() -> validator.validateDigits("1234"))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessage("숫자는 3자리 수 이상 입력할 수 없습니다.");
```

---

## 3. Phase 2 미션 수행 내용 정리

Phase 2는 이미 동작하는 나쁜 코드를 리팩토링하는 훈련으로, 테스트가 모두 Green인 상태에서 시작하며 **Green을 유지하면서 코드 구조만 개선**하는 것이 핵심이었습니다.

### 🔥 미션을 수행하며 마주친 실수와 교훈들

#### 1. 이름 짓기와 매직 넘버 — "테스트는 문서다"

**상황:** `BadUmpire`의 메서드명 `doS`, `doB`와 테스트명 `t1`, `t2`, `t3`만 보고는 이 코드가 무슨 일을 하는지 알 수 없었습니다.

```java
// ❌ Before: 무슨 테스트인지 알 수 없음
@DisplayName("테스트1")
@Test
void t1() { ... }

// ✅ After: 테스트 자체가 명세서
@DisplayName("같은 자리에 같은 수가 있으면 스트라이크이다")
@Test
void countStrike() { ... }
```

**해결 과정:** @DisplayName을 문장 형태로 작성해 @DisplayName만 읽어도 이 코드가 **무엇을 테스트하는지** 바로 파악할 수 있도록 했습니다. 테스트는 결국 팀원들과 미래의 내가 참고할 문서라는 마음가짐으로 미션을 수행했습니다.

---

#### 2. Early Return — "예외를 먼저 처리하고, 핵심만 남긴다"

**상황:** `DeepUmpire`의 `countStrike()`가 `if-else` 3단 중첩이었습니다. 핵심 로직인 for문이 들여쓰기 3칸 안쪽에 숨어 있어 코드의 흐름을 파악하기 어려웠습니다.

```java
// ❌ Before: 3단 중첩 — 핵심 로직이 어디 있는지 찾기 어려움
if (answer != null && guess != null) {
    if (answer.size() == 3 && guess.size() == 3) {
        for (...) { ... } // ← 진짜 로직은 여기
    } else {
        throw new IllegalArgumentException(...);
    }
} else {
    throw new IllegalArgumentException(...);
}

// ✅ After: 예외를 먼저 걸러내고, 핵심만 남김
if (answer == null || guess == null) throw new IllegalArgumentException(...);
if (answer.size() != 3) throw new IllegalArgumentException(...);

for (...) { ... } // ← 핵심 로직이 최상위에 드러남
```

**해결 과정:** Early Return을 적용해 코드를 읽을 때 기억할 내용을 최소한으로 줄이는 데 집중했습니다. 미션을 수행하며 IntelliJ의 `Alt + Enter` → "Invert 'if' condition"으로 조건을 자동 반전할 수 있다는 것도 알게 되었습니다.

---

#### 3. 메서드 분리 — "목차처럼 읽히는 코드"

**상황:** `MassiveUmpire`의 `playGame()`이 입력 검증, 파싱, 스트라이크 판정, 볼 판정, 결과 반환까지 **50줄이 하나의 메서드**에 들어있었습니다. 코드를 읽으려면 전체를 처음부터 끝까지 읽어야 했습니다.

```java
// ✅ After: 목차처럼 읽히는 메서드
public int[] playGame(List<String> rawAnswer, List<String> rawGuess) {
    validateInput(rawAnswer, rawGuess);
    int[] answer = parseInput(rawAnswer);
    int[] guess = parseInput(rawGuess);
    int strike = countStrike(answer, guess);
    int ball = countBall(answer, guess);
    return new int[] { strike, ball };
}
```

**해결 과정:** 주석이 없어도 코드가 글처럼 읽히는 것을 목표로 삼고, 메서드 추출을 통해 추상화 레벨을 높여서 하나의 글을 읽는 것처럼 만들었습니다. IntelliJ의 `Ctrl + Alt + M` (Extract Method)으로 코드 블록을 드래그 후 자동으로 메서드를 추출할 수 있었습니다.

---

#### 4. 관심사 분리 — "void + println = 두 가지 일을 하고 있다"

**상황:** `ConsoleUmpire`의 `play()` 메서드가 판정 결과를 반환하지 않고 바로 `System.out.println`으로 출력하고 있었습니다. 결과값을 검증할 방법이 없어 단위 테스트 작성이 불가능했습니다.

```java
// ❌ Before: 판정과 출력이 섞여 있어 테스트 불가능
public void play(answer, guess) {
    // ... 판정 로직 ...
    System.out.println("1스트라이크");  // ← 이거 때문에 테스트 불가
}

// ✅ After: 판정은 값을 반환, 출력은 별도 책임
public String judge(answer, guess) {
    // ... 판정 로직 ...
    return "1스트라이크";  // ← 이제 assertThat(result).isEqualTo("1스트라이크") 가능!
}
```

**해결 과정:** 단일 책임의 원칙을 우선으로 생각하며 미션을 수행했습니다. 예를 들어, `void`를 반환하면서 내부에서 출력까지 하고 있다면, 그 메서드는 두 가지 책임을 가지는 형태이므로 메서드 추출의 신호로 받아들이고 객체를 분리하는 형태로 구현했습니다.
