package io.ssafy.p.j14c103.homerun.api.service.character;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.character.request.CharacterSeedRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.response.CharacterSeedResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CharacterSeedServiceTest {

    @Autowired
    private CharacterSeedService characterSeedService;

    @DisplayName("MY_DATA 시작 유형이면 초기 캐시와 커리어 시드를 생성한다.")
    @Test
    void generateMyDataSeed() {
        // given
        final CharacterSeedRequest request = CharacterSeedRequest.of(
            CharacterType.MALE,
            JobType.LARGE_BIZ,
            "MY_DATA"
        );

        // when
        final CharacterSeedResponse response = characterSeedService.generate(request);

        // then
        assertThat(response.getCharacterType()).isEqualTo(CharacterType.MALE);
        assertThat(response.getSession().getJobTypeSummary()).isEqualTo(JobType.LARGE_BIZ);
        assertThat(response.getSession().getSeedType()).isEqualTo("MY_DATA");
        assertThat(response.getSession().getInitialCash()).isEqualTo(13_000_000);
        assertThat(response.getSession().getInitialNetAssets()).isEqualTo(13_000_000);
        assertThat(response.getStat().getHealth()).isEqualTo(75);
        assertThat(response.getStat().getFatigue()).isEqualTo(5);
        assertThat(response.getStat().getStress()).isEqualTo(5);
        assertThat(response.getStat().getKnowledge()).isEqualTo(50);
        assertThat(response.getStat().getHappiness()).isEqualTo(50);
        assertThat(response.getCareer().getJobTitle()).isEqualTo("수습/인턴");
        assertThat(response.getCareer().getAnnualSalary()).isEqualTo(42_000_000);
        assertThat(response.getCareer().getMonthlySalary()).isEqualTo(3_500_000);
        assertThat(response.getCareer().getEmploymentStatus()).isEqualTo(EmploymentStatus.PROBATION);
        assertThat(response.getCareer().getProbationEndTurn()).isEqualTo(6);
    }

    @DisplayName("프리랜서는 신입 직함과 재직 상태로 초기화한다.")
    @Test
    void generateFreelancerSeed() {
        // given
        final CharacterSeedRequest request = CharacterSeedRequest.of(
            CharacterType.FEMALE,
            JobType.FREELANCER,
            "PROFILE"
        );

        // when
        final CharacterSeedResponse response = characterSeedService.generate(request);

        // then
        assertThat(response.getCharacterType()).isEqualTo(CharacterType.FEMALE);
        assertThat(response.getSession().getSeedType()).isEqualTo("PROFILE");
        assertThat(response.getSession().getInitialCash()).isEqualTo(10_000_000);
        assertThat(response.getStat().getHealth()).isEqualTo(68);
        assertThat(response.getStat().getFatigue()).isEqualTo(12);
        assertThat(response.getStat().getStress()).isEqualTo(15);
        assertThat(response.getStat().getKnowledge()).isEqualTo(53);
        assertThat(response.getStat().getHappiness()).isEqualTo(55);
        assertThat(response.getCareer().getJobType()).isEqualTo(JobType.FREELANCER);
        assertThat(response.getCareer().getJobTitle()).isEqualTo("신입");
        assertThat(response.getCareer().getEmploymentStatus()).isEqualTo(EmploymentStatus.EMPLOYED);
        assertThat(response.getCareer().getProbationEndTurn()).isNull();
    }

    @DisplayName("지원하지 않는 시작 데이터 유형이면 예외가 발생한다.")
    @Test
    void generateWithInvalidSeedType() {
        // given
        final CharacterSeedRequest request = CharacterSeedRequest.of(
            CharacterType.MALE,
            JobType.MID_BIZ,
            "LEGACY"
        );

        // when & then
        assertThatThrownBy(() -> characterSeedService.generate(request))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CHARACTER_SEED_TYPE_UNSUPPORTED);
    }
}
