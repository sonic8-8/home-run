package io.ssafy.p.j14c103.homerun.api.service.game.start;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import io.ssafy.p.j14c103.homerun.api.service.game.start.response.ProfileOptionsResponse;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GameStartProfileServiceTest extends IntegrationTestSupport {

    @Autowired
    private GameStartProfileService gameStartProfileService;

    @DisplayName("시작 프로필 선택지 메타데이터를 반환한다.")
    @Test
    void getProfileOptions() {
        // when
        final ProfileOptionsResponse response = gameStartProfileService.getProfileOptions();

        // then
        assertThat(response.getProfiles())
            .extracting(
                ProfileOptionsResponse.ProfileOptionResponse::getProfileCode,
                ProfileOptionsResponse.ProfileOptionResponse::getName,
                ProfileOptionsResponse.ProfileOptionResponse::getJobType,
                ProfileOptionsResponse.ProfileOptionResponse::getAnnualSalary,
                ProfileOptionsResponse.ProfileOptionResponse::getInitialCash,
                option -> option.getStats().getSalary(),
                option -> option.getStats().getHealth(),
                option -> option.getStats().getStability(),
                option -> option.getStats().getGrowthSpeed(),
                option -> option.getStats().getDifficulty()
            )
            .containsExactly(
                tuple("JUNIOR_DEVELOPER", "신입 개발자", io.ssafy.p.j14c103.homerun.domain.character.career.JobType.MID_BIZ, 32_000_000L, 10_000_000L, 60, 70, 70, 50, 50),
                tuple("CORPORATE_OFFICE_WORKER", "대기업 사무직", io.ssafy.p.j14c103.homerun.domain.character.career.JobType.LARGE_BIZ, 42_000_000L, 10_000_000L, 80, 60, 90, 40, 70),
                tuple("IT_STARTUP", "IT 스타트업", io.ssafy.p.j14c103.homerun.domain.character.career.JobType.STARTUP, 31_000_000L, 10_000_000L, 55, 55, 35, 85, 80)
            );
    }
}
