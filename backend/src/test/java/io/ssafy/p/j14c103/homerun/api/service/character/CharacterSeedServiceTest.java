package io.ssafy.p.j14c103.homerun.api.service.character;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.character.request.CharacterSeedRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.response.CharacterSeedResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CharacterSeedServiceTest {

    private final CharacterSeedService characterSeedService = new CharacterSeedService();

    @DisplayName("MY_DATA 시작 유형이면 초기 캐시와 커리어 시드를 생성한다.")
    @Test
    void generateMyDataSeed() {
        // given
        CharacterSeedRequest request = CharacterSeedRequest.of(
            CharacterType.MALE,
            JobType.LARGE_BIZ,
            "MY_DATA"
        );

        // when
        CharacterSeedResponse response = characterSeedService.generate(request);

        // then
        assertThat(response.characterType()).isEqualTo(CharacterType.MALE);
        assertThat(response.session().jobTypeSummary()).isEqualTo(JobType.LARGE_BIZ);
        assertThat(response.session().seedType()).isEqualTo("MY_DATA");
        assertThat(response.session().initialCash()).isEqualTo(13_000_000);
        assertThat(response.session().initialNetAssets()).isEqualTo(13_000_000);
        assertThat(response.stat().health()).isEqualTo(75);
        assertThat(response.stat().fatigue()).isEqualTo(5);
        assertThat(response.stat().stress()).isEqualTo(5);
        assertThat(response.stat().knowledge()).isEqualTo(50);
        assertThat(response.stat().happiness()).isEqualTo(50);
        assertThat(response.career().jobTitle()).isEqualTo("수습/인턴");
        assertThat(response.career().annualSalary()).isEqualTo(42_000_000);
        assertThat(response.career().monthlySalary()).isEqualTo(3_500_000);
        assertThat(response.career().employmentStatus()).isEqualTo(EmploymentStatus.PROBATION);
        assertThat(response.career().probationEndTurn()).isEqualTo(6);
    }

    @DisplayName("프리랜서는 신입 직함과 재직 상태로 초기화한다.")
    @Test
    void generateFreelancerSeed() {
        // given
        CharacterSeedRequest request = CharacterSeedRequest.of(
            CharacterType.FEMALE,
            JobType.FREELANCER,
            "PROFILE"
        );

        // when
        CharacterSeedResponse response = characterSeedService.generate(request);

        // then
        assertThat(response.characterType()).isEqualTo(CharacterType.FEMALE);
        assertThat(response.session().seedType()).isEqualTo("PROFILE");
        assertThat(response.session().initialCash()).isEqualTo(10_000_000);
        assertThat(response.stat().health()).isEqualTo(68);
        assertThat(response.stat().fatigue()).isEqualTo(12);
        assertThat(response.stat().stress()).isEqualTo(15);
        assertThat(response.stat().knowledge()).isEqualTo(53);
        assertThat(response.stat().happiness()).isEqualTo(55);
        assertThat(response.career().jobType()).isEqualTo(JobType.FREELANCER);
        assertThat(response.career().jobTitle()).isEqualTo("신입");
        assertThat(response.career().employmentStatus()).isEqualTo(EmploymentStatus.EMPLOYED);
        assertThat(response.career().probationEndTurn()).isNull();
    }

    @DisplayName("지원하지 않는 시작 데이터 유형이면 예외가 발생한다.")
    @Test
    void generateWithInvalidSeedType() {
        // given
        CharacterSeedRequest request = CharacterSeedRequest.of(
            CharacterType.MALE,
            JobType.MID_BIZ,
            "LEGACY"
        );

        // when & then
        assertThatThrownBy(() -> characterSeedService.generate(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("지원하지 않는 seedType");
    }
}
